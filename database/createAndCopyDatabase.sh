# Used to create the database
rm navyDecoderDatabase.sqlite3
sqlite3 navyDecoderDatabase.sqlite3 < create_navy_decoder_tables.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_aqd_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_enlisted_rating_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_ims_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_mas_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_nec_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_nra_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_rui_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_nobc_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_officer_billet_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_officer_designator_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_officer_paygrade_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_rbsc_billet_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_ssp_codes.sql
sqlite3 navyDecoderDatabase.sqlite3 < fill_table_rp_codes.sql
cp -f   navyDecoderDatabase.sqlite3 ../navyDecoderPlus/src/main/assets/navyDecoderDatabase.sqlite3
# Likely the correct location
cp -f   navyDecoderDatabase.sqlite3 ../NavyDecoderPlus/src/main/assets/navyDecoderDatabase.sqlite3
