#!/usr/bin/env perl
use strict;
use warnings;
use JSON::PP;
use Encode qw(encode_utf8);

# Ordered list of categories and their SQL files (preserves order)
my @pairs = (
    ['AQD',                'fill_table_aqd_codes.sql'],
    ['Enlisted Rating',    'fill_table_enlisted_rating_codes.sql'],
    ['IMS',                'fill_table_ims_codes.sql'],
    ['MAS',                'fill_table_mas_codes.sql'],
    ['NEC',                'fill_table_nec_codes.sql'],
    ['NOBC',               'fill_table_nobc_codes.sql'],
    ['NRA',                'fill_table_nra_codes.sql'],
    ['Officer Billet',     'fill_table_officer_billet_codes.sql'],
    ['Officer Designator', 'fill_table_officer_designator_codes.sql'],
    ['Officer Paygrade',   'fill_table_officer_paygrade_codes.sql'],
    ['RBSC',               'fill_table_rbsc_billet_codes.sql'],
    ['RFAS-Enlisted',      'fill_table_rfas_codes_dummy.sql'],
    ['RFAS-Officer',       'fill_table_rfas_codes_dummy.sql'],
    ['RUIC',               'fill_table_rui_codes.sql'],
    ['SSP',                'fill_table_ssp_codes.sql'],
    ['RPC',                'fill_table_rp_codes.sql'],
);

my $OUTPUT_FILE = "DecoderData.json";
my @rows;

# Regex: match values( 'a','b','c' ) or values( "a","b","c" )
# - /s allows multi-line fields
# - allows doubled-quote escaping inside a field: '' or ""
my $vals_re = qr/
    \bvalues\s*\(                       # values(
      \s* (['"])                        # capture quote char in $1
      ( (?: (?:\1\1) | (?: (?!\1). ) )* )  # field1 (allows doubled quotes)
      \1 \s* , \s*
      \1 ( (?: (?:\1\1) | (?: (?!\1). ) )* ) \1 \s* , \s*
      \1 ( (?: (?:\1\1) | (?: (?!\1). ) )* ) \1
    /six;

# Tokenize file contents into statements by semicolons that are outside quotes and parentheses.
sub split_statements {
    my ($text) = @_;
    my @stmts;
    my $cur = '';
    my $inq = '';       # current quote char if inside a quote: "'" or '"', else ''
    my $paren = 0;
    my $i = 0;
    my $len = length($text);

    while ($i < $len) {
        my $ch = substr($text, $i, 1);

        if ($inq) {
            # inside a quoted literal
            if ($ch eq $inq) {
                # check for doubled-quote escape (SQL style)
                my $next = ($i+1 < $len) ? substr($text, $i+1, 1) : '';
                if ($next && $next eq $inq) {
                    # escaped quote: append both and advance
                    $cur .= $ch . $next;
                    $i += 2;
                    next;
                } else {
                    # closing quote
                    $cur .= $ch;
                    $inq = '';
                    $i++;
                    next;
                }
            } else {
                # normal char inside quote
                $cur .= $ch;
                $i++;
                next;
            }
        } else {
            # not in quote
            if ($ch eq "'" || $ch eq '"') {
                $inq = $ch;
                $cur .= $ch;
                $i++;
                next;
            } elsif ($ch eq '(') {
                $paren++;
                $cur .= $ch;
                $i++;
                next;
            } elsif ($ch eq ')') {
                $paren-- if $paren > 0;
                $cur .= $ch;
                $i++;
                next;
            } elsif ($ch eq ';') {
                # semicolon terminates a statement only if not inside parentheses
                if ($paren == 0) {
                    # finish statement
                    push @stmts, $cur;
                    $cur = '';
                    $i++;
                    next;
                } else {
                    $cur .= $ch;
                    $i++;
                    next;
                }
            } else {
                $cur .= $ch;
                $i++;
                next;
            }
        }
    }

    # push any trailing content (without trailing semicolon)
    if ($cur =~ /\S/) {
        push @stmts, $cur;
    }
    return @stmts;
}

my $skipped_files = 0;
my $malformed = 0;
my $parsed = 0;

for my $pair (@pairs) {
    my ($category, $file) = @$pair;

    unless ( -e $file ) {
        print "Skipping file (not found): $file\n";
        $skipped_files++;
        next;
    }

    open my $fh, '<:raw', $file or do {
        print "Skipping file (cannot open): $file — $!\n";
        $skipped_files++;
        next;
    };

    local $/;                # slurp whole file
    my $content = <$fh>;
    close $fh;

    my @stmts = split_statements($content);

    for my $stmt (@stmts) {
        next unless $stmt =~ /\binsert\s+into\b/i;

        my $found = 0;
        while ($stmt =~ /$vals_re/g) {
            $found = 1;
            my ($quote, $raw1, $raw2, $raw3) = ($1, $2, $3, $4);

            # Unescape doubled quotes depending on the quote char used
            if ($quote eq "'") {
                $raw1 =~ s/''/'/g;
                $raw2 =~ s/''/'/g;
                $raw3 =~ s/''/'/g;
            } else {
                $raw1 =~ s/""/"/g;
                $raw2 =~ s/""/"/g;
                $raw3 =~ s/""/"/g;
            }

            # Trim whitespace
            for ($raw1, $raw2, $raw3) {
                s/^\s+//s;
                s/\s+$//s;
            }

            push @rows, {
                categoryTitle => $category,
                codeKey       => $raw1,
                codeValue     => $raw2,
                codeSource    => $raw3,
            };
            $parsed++;
        }

        unless ($found) {
            # Malformed INSERT statement: print a short, trimmed excerpt to stdout
            my $excerpt = $stmt;
            $excerpt =~ s/^\s+|\s+$//g;           # trim
            $excerpt =~ s/\s+/ /gs;               # collapse whitespace
            $excerpt = substr($excerpt, 0, 600) . (length($excerpt) > 600 ? "..." : "");
            print "Malformed INSERT in $file: $excerpt\n";
            $malformed++;
        }
    }
}

# Write JSON using JSON::PP (safe encoding). canonical() sorts object keys so re-running this
# script with unchanged data produces a byte-identical file instead of reshuffled key order.
my $json = JSON::PP->new->ascii->pretty->canonical->encode(\@rows);
open my $out, '>:raw', $OUTPUT_FILE or die "open $OUTPUT_FILE: $!";
print $out encode_utf8($json);
close $out;

# Summary to stdout
print "\nSummary:\n";
print "  Parsed records: $parsed\n";
print "  Skipped files : $skipped_files\n";
print "  Malformed INSERTs detected: $malformed\n";
if ($malformed) {
    print "  (See the 'Malformed INSERT' lines above for examples to inspect.)\n";
}
print "Wrote " . scalar(@rows) . " records to $OUTPUT_FILE\n";