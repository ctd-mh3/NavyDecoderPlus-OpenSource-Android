#!/usr/bin/env bash
# build-db.sh - run sqlite load scripts and report any errors at the end

set -u

DB="navyDecoderDatabase.sqlite3"
ERRFILE="$(mktemp /tmp/navydb_errors.XXXXXX)"
# Clean up the temporary file on exit
trap 'rm -f "$ERRFILE"' EXIT

# Helper to run a sqlite script and capture stderr prefixed with the script name.
run_sql() {
  local sqlfile="$1"
  printf 'Running: %s\n' "$sqlfile"
  # Run sqlite3, redirect stderr to a subshell that prefixes each line and appends to $ERRFILE
  if ! sqlite3 "$DB" < "$sqlfile" 2> >(sed "s|^|[$sqlfile] |" >> "$ERRFILE"); then
    # If sqlite3 returned non-zero, write an explicit error line (helps if sqlite emits no stderr but returns non-zero)
    printf '[%s] COMMAND-EXIT-NONZERO\n' "$sqlfile" >> "$ERRFILE"
  fi
}

# Helper for other commands (e.g., cp) that you also want to validate
run_cmd() {
  local label="$1"; shift
  printf 'Running: %s\n' "$label"
  if ! "$@" 2> >(sed "s|^|[$label] |" >> "$ERRFILE"); then
    printf '[%s] COMMAND-EXIT-NONZERO\n' "$label" >> "$ERRFILE"
  fi
}

# Remove old DB
rm -f "$DB"

# Execute SQL script files (one per line)
run_sql create_navy_decoder_tables.sql
run_sql fill_table_aqd_codes.sql
run_sql fill_table_enlisted_rating_codes.sql
run_sql fill_table_ims_codes.sql
run_sql fill_table_mas_codes.sql
run_sql fill_table_nec_codes.sql
run_sql fill_table_nra_codes.sql
run_sql fill_table_rui_codes.sql
run_sql fill_table_nobc_codes.sql
run_sql fill_table_officer_billet_codes.sql
run_sql fill_table_officer_designator_codes.sql
run_sql fill_table_officer_paygrade_codes.sql
run_sql fill_table_rbsc_billet_codes.sql
run_sql fill_table_ssp_codes.sql
run_sql fill_table_rp_codes.sql

# Copy outputs (capture errors too)
run_cmd "cp -> ../navyDecoderPlus" cp -f "$DB" ../navyDecoderPlus/src/main/assets/navyDecoderDatabase.sqlite3

# Final summary
if [ -s "$ERRFILE" ]; then
  printf '\n***** WARNING: ERRORS DETECTED DURING SCRIPT RUN *****\n\n'
  printf 'There were one or more errors while running the script. Please investigate the lines below:\n\n'
  # Re-output all captured error lines
  sed -n '1,20000p' "$ERRFILE"
  printf '\n***** END ERRORS *****\n'
  exit 1
else
  printf '\nAll commands completed without reported errors.\n'
  exit 0
fi
