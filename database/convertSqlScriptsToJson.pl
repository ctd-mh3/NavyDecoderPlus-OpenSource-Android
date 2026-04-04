#!/usr/bin/perl
use strict;
use warnings;

my %INPUT_FILE_HASH = (
    'AQD' => 'fill_table_aqd_codes.sql',
    'Enlisted Rating' => 'fill_table_enlisted_rating_codes.sql',
    'IMS' => 'fill_table_ims_codes.sql',
    'MAS' => 'fill_table_mas_codes.sql',
    'NEC' => 'fill_table_nec_codes.sql',
    'NOBC' => 'fill_table_nobc_codes.sql',
    'NRA' => 'fill_table_nra_codes.sql',
    'Officer Billet' => 'fill_table_officer_billet_codes.sql',
    'Officer Designator' => 'fill_table_officer_designator_codes.sql',
    'Officer Paygrade' => 'fill_table_officer_paygrade_codes.sql',
    'RBSC' => 'fill_table_rbsc_billet_codes.sql',
    'RFAS-Enlisted' => 'fill_table_rfas_codes_dummy.sql',
    'RFAS-Officer' => 'fill_table_rfas_codes_dummy.sql',
    'RUIC' => 'fill_table_rui_codes.sql',
    'SSP' => 'fill_table_ssp_codes.sql',
    'RPC' => 'fill_table_rp_codes.sql',
);

my $OUTPUT_FILE_NAME = "DecoderData.json";
open my $OUT, ">", $OUTPUT_FILE_NAME or die "open $OUTPUT_FILE_NAME: $!";

print $OUT "[\n";

my $needComma = 0;

# JSON escape helper (robust, without external modules)
sub json_escape {
    my ($s) = @_;
    return "" unless defined $s;
    # backslash and double-quote
    $s =~ s/\\/\\\\/g;
    $s =~ s/"/\\"/g;
    # control characters
    $s =~ s/\x08/\\b/g;   # backspace (fix: match the actual control char)
    $s =~ s/\f/\\f/g;     # formfeed
    $s =~ s/\n/\\n/g;
    $s =~ s/\r/\\r/g;
    $s =~ s/\t/\\t/g;
    # escape any other C0 control chars as \uXXXX
    $s =~ s/([\x00-\x1f])/sprintf("\\u%04x", ord($1))/eg;
    return $s;
}

# Pattern: match values('a','b','c') or values("a","b","c") and allow doubled quotes inside
my $pattern = qr/
    \bvalues\s*\(                     # values (
      \s* (['"])                      # opening quote captured in $1
      ( (?:(?:(?!\1).)|\1\1)* )       # field1 in $2
      \1 \s* , \s*
      \1 ( (?:(?:(?!\1).)|\1\1)* )    # field2 in $3
      \1 \s* , \s*
      \1 ( (?:(?:(?!\1).)|\1\1)* )    # field3 in $4
      \1
    /ix;

for my $key ( sort keys %INPUT_FILE_HASH ) {   # sorted for deterministic output
    my $fileName = $INPUT_FILE_HASH{$key};
    open my $FH, "<", $fileName or do {
        warn "Could not open $fileName: $!\n";
        next;
    };

    while (my $line = <$FH>) {
        chomp $line;
        if ($line =~ /$pattern/) {
            my ($quote, $raw1, $raw2, $raw3) = ($1, $2, $3, $4);

            # Unescape doubled quotes depending on which quote was used
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
            for my $ref (\$raw1, \$raw2, \$raw3) {
                $$ref =~ s/^\s+//;
                $$ref =~ s/\s+$//;
            }

            # JSON-escape values
            my $j_cat = json_escape($key);
            my $j_key = json_escape($raw1);
            my $j_val = json_escape($raw2);
            my $j_src = json_escape($raw3);

            print $OUT ",\n" if $needComma;
            $needComma = 1;

            print $OUT "  { \"categoryTitle\": \"$j_cat\", ";
            print $OUT "\"codeKey\": \"$j_key\", ";
            print $OUT "\"codeValue\": \"$j_val\", ";
            print $OUT "\"codeSource\": \"$j_src\" }";
        }
    }
    close $FH;
}

print $OUT "\n]\n";
close $OUT;
