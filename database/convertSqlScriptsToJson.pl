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
# - Uses /s so the quoted field can contain newlines
# - Allows doubled-quote escaping inside a field: '' or ""
my $vals_re = qr/
    \bvalues\s*\(                       # values(
      \s* (['"])                        # capture quote char in $1
      ( (?: (?:\1\1) | (?: (?!\1). ) )* )  # field1 (allows doubled quotes)
      \1 \s* , \s*
      \1 ( (?: (?:\1\1) | (?: (?!\1). ) )* ) \1 \s* , \s*
      \1 ( (?: (?:\1\1) | (?: (?!\1). ) )* ) \1
    /six;

for my $pair (@pairs) {
    my ($category, $file) = @$pair;
    open my $fh, '<:raw', $file or do {
        warn "Warning: cannot open $file: $!\n";
        next;
    };
    local $/;                # slurp whole file
    my $content = <$fh>;
    close $fh;

    while ($content =~ /$vals_re/g) {
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

        # Optional: trim leading/trailing whitespace
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
    }
}

# Write JSON using JSON::PP (safe encoding)
my $json = JSON::PP->new->ascii->pretty->encode(\@rows);
open my $out, '>:raw', $OUTPUT_FILE or die "open $OUTPUT_FILE: $!";
print $out encode_utf8($json);
close $out;

print "Wrote " . scalar(@rows) . " records to $OUTPUT_FILE\n";
