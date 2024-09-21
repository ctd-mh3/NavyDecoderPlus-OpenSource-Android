

# [{ "category": "MAS", "codeKey": "AAP", "codeValue": "Admin Action", "codeSource": "NRH Page" },
#  { "category": "MAS", "codeKey": "ARR", "codeValue": "Retirement Pending", "codeSource": "NRH Page" },
#  { "category": "IMS", "codeKey": "D3G", "codeValue": "4-30 day granted", "codeSource": "NRH Page" },
#  { "category": "IMS", "codeKey": "D3P", "codeValue": "4-30 delay pending", "codeSource": "NRH Page"} ]



#my @INPUT_FILE_NAME_ARRAY;
#push(@INPUT_FILE_NAME_ARRAY, "fill_table_ims_codes.sql");
my %INPUT_FILE_HASH = ();
$INPUT_FILE_HASH{'AQD'} = 'fill_table_aqd_codes.sql';
$INPUT_FILE_HASH{'Enlisted Ratings'} = 'fill_table_enlisted_rating_codes.sql';
$INPUT_FILE_HASH{'IMS'} = 'fill_table_ims_codes.sql';
$INPUT_FILE_HASH{'MAS'} = 'fill_table_mas_codes.sql';
$INPUT_FILE_HASH{'NEC'} = 'fill_table_nec_codes.sql';
$INPUT_FILE_HASH{'NOBC'} = 'fill_table_nobc_codes.sql';
$INPUT_FILE_HASH{'NRA'} = 'fill_table_nra_codes.sql';
$INPUT_FILE_HASH{'Officer Billet'} = 'fill_table_officer_billet_codes.sql';
$INPUT_FILE_HASH{'Officer Designator'} = 'fill_table_officer_designator_codes.sql';
$INPUT_FILE_HASH{'Officer Paygrade'} = 'fill_table_officer_paygrade_codes.sql';
$INPUT_FILE_HASH{'RBSC'} = 'fill_table_rbsc_billet_codes.sql';
$INPUT_FILE_HASH{'RFAS-Enlisted'} = 'fill_table_rfas_codes_dummy.sql';
$INPUT_FILE_HASH{'RFAS-Officer'} = 'fill_table_rfas_codes_dummy.sql';
$INPUT_FILE_HASH{'RUIC'} = 'fill_table_rui_codes.sql';
$INPUT_FILE_HASH{'SSP'} = 'fill_table_ssp_codes.sql';
$INPUT_FILE_HASH{'RPC'} = 'fill_table_rp_codes.sql';


my $OUTPUT_FILE_NAME = "DecoderData.json";
open OUTPUT_FILE, ">", $OUTPUT_FILE_NAME or die $!;


# Output header for JSON file
print OUTPUT_FILE "[\n";


my $needComma = 0;

# For each input file output JSON information
for my $key ( keys %INPUT_FILE_HASH ) {
	my $fileName = $INPUT_FILE_HASH{$key};

		
	open FILE, "<", $fileName or die $!;

	while (<FILE>) {
		my $line = $_;

		if ($line =~ /^insert into.*values \(\"(.*)\",\"(.*)\",\"(.*)\".*/) {
			
			if ($needComma) {
				print OUTPUT_FILE  ",\n";
			}
			else {
				$needComma = 1;
		    }
			print OUTPUT_FILE "{ \"categoryTitle\": \"$key\", ";
			print OUTPUT_FILE  "\"codeKey\": \"$1\", ";
			print OUTPUT_FILE  "\"codeValue\": \"$2\", ";
			print OUTPUT_FILE  "\"codeSource\": \"$3\"}";
		}
	}
}

# Output footer for JSON file
print OUTPUT_FILE "\n]\n";


