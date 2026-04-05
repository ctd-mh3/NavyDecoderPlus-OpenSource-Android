.echo on
.mode column
.headers on
.nullvalue NULL

CREATE TABLE android_metadata (locale text default 'en_US');
INSERT INTO android_metadata VALUES ('en_US');

CREATE VIRTUAL TABLE FTS_enlisted_rating_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_ims_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_mas_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_nra_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_rui_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_nec_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_nobc_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_officer_billet_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_officer_designator_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_officer_paygrade_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_rbsc_billet_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_ssp_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_rp_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

CREATE VIRTUAL TABLE FTS_aqd_codes USING fts3 (
  suggest_text_1, 
  suggest_text_2,
  source);

/* Historical non-FTS table format, kept for reference:
create table enlisted_rating_codes (
  _id integer primary key,
  rating_code text unique check (length(rating_code) <= 4) not null,
  rating_description text not null,
  source text not null);
*/
