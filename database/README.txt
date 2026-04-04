*************************************************************************************

General Approach: see http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/

*************************************************************************************

General Steps to Update the Database Which the App Uses:
- Update sql files in this directory when new information is available
  + Typically done manually via text editor
  + Typically after Navy Decoder (not Plus) app is updated
    - Enlisted Ratings: https://www.mynavyhr.navy.mil/References/NEOCS-Manual/NEOCS-Vol-I/
- Run createAndCopyDatabase.sh
- Ensure DB_VERSION is incremented in DecodeDatabase.java file
- For iOS version of app 
  + run "perl convertSqlScriptsToJson.pl"



















