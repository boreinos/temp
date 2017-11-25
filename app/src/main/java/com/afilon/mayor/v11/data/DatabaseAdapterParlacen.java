package com.afilon.mayor.v11.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Log;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.model.BallotResults;
import com.afilon.mayor.v11.model.Candidate;
import com.afilon.mayor.v11.model.CandidateMarks;
import com.afilon.mayor.v11.model.CrossVoteBundle;
import com.afilon.mayor.v11.model.Party;
import com.afilon.mayor.v11.model.PreferentialCandidateVotes;
import com.afilon.mayor.v11.model.elsaLog;
import com.afilon.mayor.v11.model.PreferentialPartyVotes;
import com.afilon.mayor.v11.model.PreferentialVotoBanderas;
import com.afilon.mayor.v11.model.PresidenteStaff;
import com.afilon.mayor.v11.model.VotingCenter;
import com.afilon.mayor.v11.utils.CollectionUtils;
import com.afilon.mayor.v11.model.User;
import com.afilon.mayor.v11.utils.Consts;

public class DatabaseAdapterParlacen {

    private static final String PREFERENTIAL_ELECTION_PARTIES_TABLE = "direct_election_parties";
    private static final String ELECTION_CANDIDATES_TABLE = "preferential_election_candidates";
    private static final String PREFERENTIAL_ELECTION_PARTY_VOTES = "preferential_election_party_votes";
    private static final String PREFERENTIAL_ELECTION_BANDERA_VOTES = "preferential_election_bandera_votes";
    private static final String DIRECT_ELECTION_PARTIES="direct_election_parties";
    private static final String CANDIDATE_TOTALMARKS = "candidate_total_marks";
    private static final String ACTA_QUALITY_CATALOG = "ActaQualityCatalog";
    private static final String ACTA_QUALITY_REPORT = "ActaQualityReport";
    private static final String Attendees = "ActaMembers";

    /* columns for mesa/junta members table */
    private static final String USERID = "DUI";
    private static final String USERNAME = "Name";
    private static final String USERPARTY = "Party";
    private static final String SIGNIN = "SignedIn";
    private static final String TITLE = "Title";
    private static final String PARTY = "Party";
    private static final String CONFIRMED = "Validated";
    private static final String PROPRIETARIO = "Proprietario";
    private static final String JRV = "JRV";
    private static final String MERMembers = "MESAMembers";

    private static final String PREFERENTIAL_ELECTION_CANDIDATE_VOTES = "preferential_election_candidate_votes";
    private static final String PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES = "concepts_count_preferential";
    private static final String PREFERENTIAL_CANDIDATE_CROSSVOTES = "candidate_crossvotes";
    private static final String CANDIDATE_PREFERENTIALVOTES = "pref_vote_temp"; //used for ES only, duirng crossvote activity! currently used as temp only
    private static final String CANDIDATE_PLANCHAVOTES = "plancha_vote_temp";  // used for ES only during crossvote activity, temp only
    private static final String ELECTION_TYPES = "ElectionTypes";
    private static final String MARKS = "Marks";
    private static final String CANDIDATE_MARKS = "";
    private static final String LOGIN = "login_verification";
    private static final String APPLOG = "APPLOG";
    private static final String ACTA_SIGNATURES_TABLE = "direct_jrv_accept";
    public static final String PRESIDENT = "Presidente";
    public static final String SECRETARIO = "Secretario";
    public static final String SECRETARIOHON = "secretario";

    private static final String DEBUG_TAG = "Database Adapter";

    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;


    public DatabaseAdapterParlacen(Context context) {
        this.context = context;

    }


    public DatabaseAdapterParlacen open() throws SQLException {

        dbHelper = DatabaseHelper.getInstance(context);

        database = dbHelper.getWritableDatabase();

        return this;
    }

    public void close() {
        dbHelper.close();
        database.close();
    }

    public void backupDatabase() {
        dbHelper.backUpDataBase();
    }

    /**
     * Check if table exist and return boolean as response
     * @param tableName Table name
     * @param openDb
     * @return
     */
    public boolean hasTable(String tableName, boolean openDb) {
        if(openDb) {
            if(database == null || !database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }

            if(!database.isReadOnly()) {
                database.close();
                database = dbHelper.getWritableDatabase();
            }
        }

        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /**
     *
     * @param tables table list
     * @return
     */
    public boolean hasTables(String... tables) {
        boolean isFound = true;
        for (String tbl: tables) {
            if(! hasTable(tbl, true)) { isFound = false; }
        }
        return isFound;
    }

    public String getDui(String user) {
        Cursor cursor = database.query(Attendees, new String[]{"MRE","usr_id","usr_title","present"},null,null,null,null,null);

        while (cursor.moveToNext()) {
            if(cursor.getString(cursor.getColumnIndexOrThrow("usr_title")).equals(user)){
                String usrId =cursor.getString(cursor.getColumnIndexOrThrow("usr_id"));
                cursor.close();
                return usrId;
            }
        }
        cursor.close();
        return "";
    }

    public boolean verifyDui(String user, String dui, String jrv) {
//        if(Consts.LOCALE.contains("ELSA")) {
            Cursor cursor = database.query(Attendees, new String[]{"MRE", "usr_id", "usr_title", "present"}, null, null, null, null, null);

            while (cursor.moveToNext()) {

                if (cursor.getString(cursor.getColumnIndexOrThrow("usr_title")).equals(user)) {
                    return cursor.getString(cursor.getColumnIndexOrThrow("usr_id")).equals(dui);
                }
            }
        cursor.close();
            return false;
//        } else if (Consts.LOCALE.contains("HON")){
//            String confirmation ="";
//            String tablet = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
//            String id = scrambleId(tablet, jrv);
//            String sqlQuery = "SELECT CASE WHEN " + "exists( SELECT tablet FROM "+LOGIN+ " WHERE usuario =? and dui=? and jrv=? and tablet=? "+ ") then tablet else 'false' end "+ " as confirmation from "+LOGIN+ " WHERE usuario =? and dui=? and jrv=? and tablet=?";
//            Cursor cursor = database.rawQuery(sqlQuery, new String[]{user, dui, jrv, id,user, dui, jrv, id});
//            while (cursor.moveToNext()){
//                confirmation = cursor.getString(cursor.getColumnIndexOrThrow("confirmation"));
//            }
//            return (scrambleId(confirmation, jrv)).equals(tablet);
//        } else{
//            return false;
//        }

    }

    private String scrambleId(String tablet, String key) {
        byte[] list = tablet.getBytes();
        byte[] twolist = key.getBytes();
        //todo find a better way to scramble
//		for (int i = 0; i< list.length; i++){
//			list[i] ^= twolist[0];
//		}
        String newId = new String(list);
        return newId;
    }

    public void insertDui(String jrv, String pDui, String sDui) {
        insertUser(jrv, pDui, PRESIDENT);
        insertUser(jrv, sDui, "secretario");

    }

    private void insertUser(String jrv, String dui, String user) {
        String tablet = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        String id = scrambleId(tablet, jrv);
        Log.e("SCRAMBLE:", id);
        ContentValues contentValues = new ContentValues();
        contentValues.put("JRV", jrv);
        contentValues.put("usuario", user);
        contentValues.put("dui", dui);
        contentValues.put("tablet", id);
        database.insert(LOGIN, null, contentValues);
    }

    public VotingCenter getNewJrv(String id) {

        String sqlQuery = "SELECT  *  FROM voting_center WHERE  CAST([Start_JRV] as integer) <= ? AND  CAST([End_JRV] as integer) >= ?";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{id, id});

        VotingCenter newJrv = new VotingCenter();
        newJrv.setJrvString(id);
        int columnNumber = 5; //default
        switch (context.getResources().getString(R.string.electionType)) {
            case Consts.MAYOR:
                columnNumber = 5;
                break;
            case Consts.ASAMBLEA:
                columnNumber = 7;
                break;
            case Consts.PRESIDENT:
                columnNumber = 6;
                break;
            case Consts.PARLACEN:
                columnNumber = 8;
                break;
            default:
                //todo throw error, election type must be defined.
                break;

        }


        if (cursor != null && cursor.moveToFirst()) {
//			newJrv.setPreferential_election2_id(cursor.getString(5));
            newJrv.setJrvid(id);
            newJrv.setDepartmentoString(cursor.getString(2));
            newJrv.setMunicipioString(cursor.getString(3));
            newJrv.setVoteCenterString(cursor.getString(4));
            //newJrv.setVoters(cursor.getString(5));
            //newJrv.setVc_Direct_Election_id(cursor.getString(9));
            //newJrv.setDirect_Election_id(cursor.getString(10));
//			newJrv.setVc_Preferential_Election_id(cursor.getString(5));
            newJrv.setPreferential_Election_id(cursor.getString(columnNumber));

            cursor.close();
        }
        return newJrv;
    }

    public void deleteAllLogin() {
        database.delete(LOGIN, null, null);
    }

    public ArrayList<Party> getParlacenPartiesArrayList(String pref_election_id2) {
        ArrayList<Party> list = new ArrayList<Party>();
        String sqlQuery = "";
        String electionIdColumn = "";
        String partyOrderColumn = "Party_Order";
        switch (context.getResources().getString(R.string.electionType)) {
            case Consts.MAYOR:
                sqlQuery = "select * from direct_election_parties where CAST(Direct_Election_ID AS INTEGER) =?" + " ORDER BY CAST(party_order AS INTEGER)";
                electionIdColumn = "Party_Direct_Election_ID";
                break;
            case Consts.ASAMBLEA:
                sqlQuery = "SELECT Party_Preferential_Election_ID, Party, party_order" +
                        " FROM preferential_election_parties" +
                        " WHERE Preferential_Election_ID=? " +
                        " ORDER BY CAST(party_order AS INTEGER)";
                electionIdColumn = "Party_Preferential_Election_ID";
                partyOrderColumn = "Party_Order";
                break;
            case Consts.PRESIDENT:
                sqlQuery="select * from direct_election_parties where CAST(Direct_Election_ID AS INTEGER) =?"+ " ORDER BY CAST(party_order AS INTEGER)";
                electionIdColumn = "Party_Direct_Election_ID";
                break;
            case Consts.PARLACEN:
                break;
            default:
                break;
        }

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{pref_election_id2});
        if (cursor.moveToFirst()) {
            do {

                String party_id = pref_election_id2;  //cursor.getString(cursor
                String party_preferential_election_id = cursor
                        .getString(cursor
                                .getColumnIndexOrThrow(electionIdColumn));
                String party_name = cursor.getString(cursor
                        .getColumnIndexOrThrow("Party"));
                String party_order = cursor.getString(cursor
                        .getColumnIndexOrThrow(partyOrderColumn));
                Party cls = new Party(party_id, pref_election_id2,
                        party_preferential_election_id, party_name, party_order);
                list.add(cls);
            } while (cursor.moveToNext());

        }
        cursor.close();
        return list;
    }

    public ArrayList<Candidate> getParlacenCandidatesArrayList(String party_preferential_election_id){
        if(Consts.LOCALE.contains("ELSA")){
            return  getCandidatesArrayListSV(party_preferential_election_id);
        }
        return getCandidatesArrayListHN(party_preferential_election_id);
    }

    public ArrayList<Candidate> getCandidatesArrayListSV(
            String party_preferential_election_id) {
        ArrayList<Candidate> list = new ArrayList<Candidate>();
        String sqlQuery = "Select Party, Candidates, Candidate_Preferential_Election_ID,"
                + "Party_Preference_Rank " +
                " from " + ELECTION_CANDIDATES_TABLE +
                " WHERE Party_Preferential_Election_ID=? " +
                " ORDER BY  CAST(party_preference_rank as integer)";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{party_preferential_election_id});
        if (cursor.moveToFirst()) {
            do {
                String party_name = cursor.getString(cursor.getColumnIndexOrThrow("Party"));
                String candidate_name = cursor.getString(cursor.getColumnIndexOrThrow("Candidates"));
                String candidate_id = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_Preferential_Election_ID"));
                String candidate_order = cursor.getString(cursor.getColumnIndexOrThrow("Party_Preference_Rank"));

                Candidate cls = new Candidate(candidate_name, candidate_order,
                        party_name, candidate_id, candidate_order);

                cls.setPreferentialVotes(0.0f);
                cls.setVotesNumber(0.0f);
                cls.setBanderaNumber(0.0f);
                cls.setPartyPreferentialElectionID(party_preferential_election_id);
                list.add(cls);
            } while (cursor.moveToNext());

        }
        cursor.close();

        return list;
    }

    public ArrayList<Candidate> getCandidatesArrayListHN(
            String party_preferential_election_id) {
        ArrayList<Candidate> list = new ArrayList<Candidate>();
        String sqlQuery = "Select Party, Candidates, Candidate_Preferential_Election_ID,"
                + "Party_Preference_Rank , Candidate_index " +
                " from " + ELECTION_CANDIDATES_TABLE +
                " WHERE Party_Preferential_Election_ID=? " +
                " ORDER BY  CAST(party_preference_rank as integer)";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{party_preferential_election_id});
        if (cursor.moveToFirst()) {
            do {
                String party_name = cursor.getString(cursor.getColumnIndexOrThrow("Party"));
                String candidate_name = cursor.getString(cursor.getColumnIndexOrThrow("Candidates"));
                String candidate_id = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_Preferential_Election_ID"));
                String candidate_order = cursor.getString(cursor.getColumnIndexOrThrow("Party_Preference_Rank"));
                String candidate_index = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_index"));

                Candidate cls = new Candidate(candidate_name, candidate_index,
                        party_name, candidate_id, candidate_order);

                cls.setPreferentialVotes(0.0f);
                cls.setVotesNumber(0.0f);
                cls.setBanderaNumber(0.0f);
                cls.setPartyPreferentialElectionID(party_preferential_election_id);
                list.add(cls);
            } while (cursor.moveToNext());

        }
        cursor.close();

        return list;
    }

    //----------------------------------------------------------------------------------------------
    // cross vote summary and Resumen :
    public ArrayList<Candidate> getCandidatesWithMarks(String party_election_id) {
        String candidateIndex = getCandidateIndexColumnName();
        ArrayList<Candidate> candidates = new ArrayList<>();
        String slqQuery = "select " +
                "  u.Candidate_Preferential_Election_ID," +
                "  u.Preferential_Election_ID," +
                "  u.Party_Preferential_Election_ID," +
                "  u.Candidates," +
//                "  u.Party_Preference_Rank," +
                candidateIndex +
                "  u.Party," +
                "  ifnull(sum(s4.TotalMarks),'0') as CrossMarks" +
                "  from" +
                "  preferential_election_candidates u" +
                "  left outer join  Marks s4 on" +
                "  u.Candidate_Preferential_Election_ID = s4.CandidateId" +
                "  and  s4.ElectionType = 6 " +
                "  where u.Party_Preferential_Election_ID = ?" +
                "  GROUP BY u.Candidate_Preferential_Election_ID" +
                "  ORDER BY  CAST(u.party_preference_rank as integer)";
        Cursor cursor = database.rawQuery(slqQuery, new String[]{party_election_id});
        if (cursor.moveToNext()) {
            do {
                String candidateName = cursor.getString(cursor.getColumnIndexOrThrow("Candidates"));
                String candidateID = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_Preferential_Election_ID"));
                String partyName = cursor.getString(cursor.getColumnIndexOrThrow("Party"));
                String crossMarks = cursor.getString(cursor.getColumnIndexOrThrow("CrossMarks"));
                String candidateRank = cursor.getString(cursor.getColumnIndexOrThrow("Party_Preference_Rank"));
                Candidate candidate = new Candidate(candidateName, partyName, candidateID, party_election_id);
                candidate.setMarcas(Integer.valueOf(crossMarks));
                candidate.setPreferentialMarks(0);
                candidate.setCrossVote(0.0f);
                candidate.setCandidate_order(candidateRank);
                candidate.setPreferentialVotes(0.0f);
                candidate.setVotesNumber(0.0f);
                candidate.setBanderaNumber(0.0f);
                candidates.add(candidate);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return candidates;
    }

    private String getCandidateIndexColumnName(){
        String candidateIndex = "throwError";
        // todo find a more elegant solution
        if(Consts.LOCALE.equals(Consts.ELSALVADOR)){
            candidateIndex = " u.Party_Preference_Rank,";
        }else if(Consts.LOCALE.equals(Consts.HONDURAS)){
            candidateIndex = " u.Candidate_index as Party_Preference_Rank,";
        }
        return  candidateIndex;
    }

    public ArrayList<Candidate> getCandidatesWithTotalMarks(String party_election_id) {
        String candidateIndex = getCandidateIndexColumnName();
        ArrayList<Candidate> candidates = new ArrayList<>();
        String slqQuery = "select " +
                "  u.Candidate_Preferential_Election_ID," +
                "  u.Preferential_Election_ID," +
                "  u.Party_Preferential_Election_ID," +
                "  u.Candidates," +
//                "  u.Party_Preference_Rank," +
                candidateIndex +
                "  u.Party," +
                "  ifnull(sum(s4.TotalMarks),'0') as totalMarks" +
                "  from" +
                "  preferential_election_candidates u" +
                "  left outer join  Marks s4 on" +
                "  u.Candidate_Preferential_Election_ID = s4.CandidateId" +
                "  where u.Party_Preferential_Election_ID = ?" +
                "  GROUP BY u.Candidate_Preferential_Election_ID" +
                "  ORDER BY  CAST(u.party_preference_rank as integer)";
        Cursor cursor = database.rawQuery(slqQuery, new String[]{party_election_id});
        if (cursor.moveToNext()) {
            do {
                String candidateName = cursor.getString(cursor.getColumnIndexOrThrow("Candidates"));
                String candidateID = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_Preferential_Election_ID"));
                String partyName = cursor.getString(cursor.getColumnIndexOrThrow("Party"));
                String crossMarks = cursor.getString(cursor.getColumnIndexOrThrow("totalMarks"));
                String candidateRank = cursor.getString(cursor.getColumnIndexOrThrow("Party_Preference_Rank"));
                Candidate candidate = new Candidate(candidateName, partyName, candidateID, party_election_id);
                candidate.setTotalMarks(0, Integer.valueOf(crossMarks));
                candidate.setCrossVote(0.0f);
                candidate.setCandidate_order(candidateRank);
                candidate.setPreferentialVotes(0.0f);
                candidate.setVotesNumber(0.0f);
                candidate.setBanderaNumber(0.0f);
                candidates.add(candidate);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return candidates;

    }

    public ArrayList<Candidate> getCandidateTotalVotes(ArrayList<Candidate> candidates, String party_election_id) {
        ArrayList<Candidate> updatedList = candidates;
        String slqQuery = "select " +
                "  u.Candidate_Preferential_Election_ID," +
                " c.JRV, " +
                " c.Party_Preferential_Election_ID," +
                " c.Candidate_Preferential_Election_ID," +
                " c.Candidate_Votes, " +
                " c.Candidate_Bandera_Votes," +
                " c.candidate_bandera_marks," +
                " c.Candidate_Preferential_Votes," +
                " c.Preferential_Election_ID," +
                " c.candidate_cross_votes as crossVotes" +
                "  from" +
                "  preferential_election_candidates u" +
                "  left outer join  preferential_election_candidate_votes c on " +
                "  u.Candidate_Preferential_Election_ID = c.Candidate_Preferential_Election_ID" +
                "  where u.Party_Preferential_Election_ID = ?" +
                "  GROUP BY u.Candidate_Preferential_Election_ID" +
                "  ORDER BY  CAST(u.party_preference_rank as integer)";
        Cursor cursor = database.rawQuery(slqQuery, new String[]{party_election_id});
        int i = 0;
        if (cursor.moveToNext()) {
            do {
                Candidate candidate = updatedList.get(i);
                //Float crossVotes = cursor.getFloat(cursor.getColumnIndexOrThrow("crossVotes"));
                Float prefVotes = cursor.getFloat(cursor.getColumnIndexOrThrow("Candidate_Preferential_Votes"));
                Float bandVotes = cursor.getFloat(cursor.getColumnIndexOrThrow("Candidate_Bandera_Votes"));
                int bandMarks = cursor.getInt(cursor.getColumnIndexOrThrow("candidate_bandera_marks"));
                // candidate.setCrossVote(Float.valueOf(crossVotes));
                candidate.setPreferentialVotes(prefVotes);
                candidate.setBanderaNumber(bandVotes);
                candidate.setBanderaMarks(bandMarks);
                candidate.updateTotalVotes();
                updatedList.set(i, candidate);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return updatedList;

    }

    public ArrayList<Candidate> getCandidateCrossVotes(ArrayList<Candidate> candidates, String party_election_id) {
        ArrayList<Candidate> updatedList = candidates;
        String slqQuery = "select " +
                "  u.Candidate_Preferential_Election_ID," +
                "  ifnull(sum(cV.Candidate_CrossVotes),'0.0') as crossVotes" +
                "  from" +
                "  preferential_election_candidates u" +
                "  left outer join  candidate_crossvotes cV on " +
                "  u.Candidate_Preferential_Election_ID = cV.Candidate_Pref_Elec_ID" +
                "  where u.Party_Preferential_Election_ID = ?" +
                "  GROUP BY u.Candidate_Preferential_Election_ID" +
                "  ORDER BY  CAST(u.party_preference_rank as integer)";
        Cursor cursor = database.rawQuery(slqQuery, new String[]{party_election_id});
        int i = 0;
        if (cursor.moveToNext()) {
            do {
                Candidate candidate = updatedList.get(i);
                String crossVotes = cursor.getString(cursor.getColumnIndexOrThrow("crossVotes"));
                candidate.setCrossVote(Float.valueOf(crossVotes));
                updatedList.set(i, candidate);
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return updatedList;
    }

    public float getPartyCrossVotes(String party_election_id) {
        float crossVotes = 0.0f;
        String sqlQuery = "SELECT " +
                " u.Party_Preferential_Election_ID," +
                " ifnull(SUM(s3.Candidate_CrossVotes),'0.0') as partyVotes " +
                " FROM preferential_election_parties u" +
                " left outer join  candidate_crossvotes s3 on" +
                " u.Party_Preferential_Election_ID = s3.Party_Pref_Elec_ID" +
                " where u.Party_Preferential_Election_ID  = ?" +
                " GROUP BY u.Party_Preferential_Election_ID  ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{party_election_id});
        if (cursor.moveToNext()) {
            String votes = cursor.getString(cursor.getColumnIndexOrThrow("partyVotes"));
            crossVotes = Float.valueOf(votes);
        }
        cursor.close();
        return crossVotes;
    }

    public float getTotalPartyVotes(String partyID) {
        float totalVotes = 0.0f;
        String[] tableColumns = new String[]{"jrv",
                "preferential_election_id", "bandera_preferential_election_id",
                "party_preferential_election_id", "party_votes",
                "party_boletas", "Party_Preferential_Votes", "party_cross_votes"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_BANDERA_VOTES,
                tableColumns, "party_preferential_election_id=?", new String[]{partyID}, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            float cross = cursor.getFloat(7);
            float pref = cursor.getFloat(6);
            float band = cursor.getFloat(4);
            totalVotes = cross + pref + band;
            cursor.moveToNext();
        }
        cursor.close();
        return totalVotes;
    }

    public float getBanderaVotes(String partyID){
        float totalVotes = 0.0f;
        String[] tableColumns = new String[]{"jrv",
                "preferential_election_id", "bandera_preferential_election_id",
                "party_preferential_election_id", "party_votes",
                "party_boletas", "Party_Preferential_Votes", "party_cross_votes"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_BANDERA_VOTES,
                tableColumns, "party_preferential_election_id=?", new String[]{partyID}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
//            float cross = cursor.getFloat(7);
//            float pref = cursor.getFloat(6);
//            float band = cursor.getFloat(4);
            totalVotes = cursor.getFloat(4);
            cursor.moveToNext();
        }
        cursor.close();
        return totalVotes;
    }

    public float getPreferentialVotes(String partyID){
        float totalVotes = 0.0f;
        String[] tableColumns = new String[]{"jrv",
                "preferential_election_id", "bandera_preferential_election_id",
                "party_preferential_election_id", "party_votes",
                "party_boletas", "Party_Preferential_Votes", "party_cross_votes"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_BANDERA_VOTES,
                tableColumns, "party_preferential_election_id=?", new String[]{partyID}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
//            float cross = cursor.getFloat(7);
//            float pref = cursor.getFloat(6);
//            float band = cursor.getFloat(4);
            totalVotes = cursor.getFloat(6);
            cursor.moveToNext();
        }
        cursor.close();
        return totalVotes;
    }

    public void insertBallotResult(int ballotCount,int marcas,float votes,String partyName,String jrv, String pref_election_ID, String party_election_id){

        ContentValues contentValues = new ContentValues();
        contentValues.put("Papeleta", ballotCount);
        contentValues.put("Marcas", marcas);
        contentValues.put("Votos", votes);
        contentValues.put("Partido",partyName);
        contentValues.put("JRV", jrv);
        contentValues.put("Preferential_Election_ID",pref_election_ID);
        contentValues.put("Party_Preferential_Election_ID",party_election_id);

        database.insert("Papeleta_Outcome", null, contentValues);
    }

    public ArrayList<BallotResults> getBallotResults(){
        Log.e("Narrator","About to retrieve Ballot results");
        ArrayList<BallotResults> ballotResults = new ArrayList<>();
        String[] tableColumns = new String[]{"Papeleta","Marcas","Votos","Partido","JRV"};
        Cursor cursor = database.query("Papeleta_Outcome",tableColumns,null,null,null,null,"Papeleta");
        Log.e("Narrator","Query was performed sucessful");
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            int papeletaCount = cursor.getInt(0);
            int marcas = cursor.getInt(1);
            float votes = cursor.getFloat(2);
            String partyName = cursor.getString(3);
            String jrv = cursor.getString(4);
            BallotResults result = new BallotResults(papeletaCount,marcas,votes,partyName,jrv);
            ballotResults.add(result);
            cursor.moveToNext();
        }
        cursor.close();
        return ballotResults;
    }
    public ArrayList<BallotResults> getFinalBallotResults(){
        Log.e("Narrator","About to retrieve Ballot results");
        ArrayList<BallotResults> ballotResults = new ArrayList<>();
        String[] tableColumns = new String[]{"Papeleta","Marcas","Votos","Partido","JRV","Preferential_Election_ID","Party_Preferential_Election_ID"};
        Cursor cursor = database.query("Papeleta_Outcome",tableColumns,null,null,null,null,"Papeleta");
        Log.e("Narrator","Query was performed sucessful");
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            int papeletaCount = cursor.getInt(0);
            int marcas = cursor.getInt(1);
            float votes = cursor.getFloat(2);
            String partyName = cursor.getString(3);
            String jrv = cursor.getString(4);
            String electionId = cursor.getString(5);
            String partyId = cursor.getString(6);
            BallotResults result = new BallotResults(papeletaCount,marcas,votes,partyName,jrv);
            result.setPreferential_Election_ID(electionId);
            result.setParty_Preferential_Election_ID(partyId);
            ballotResults.add(result);
            cursor.moveToNext();
        }
        cursor.close();
        return ballotResults;
    }

    public void deleteBallotResults(){
        database.delete("Papeleta_Outcome",null,null);
    }

    public void updateCandidateFinalVote(Candidate candidate) {
        String candidateEleID = candidate.getCandidatePreferentialElectionID();
        ContentValues contentValues = new ContentValues();
        //contentValues.put("JRV", jrv);
        contentValues.put("Party_Preferential_Election_ID", candidate.getPartyPreferentialElectionID());
        contentValues.put("Candidate_Preferential_Election_ID", candidateEleID);
        contentValues.put("Candidate_Votes", candidate.getVotesNumber());
        contentValues.put("Candidate_Bandera_Votes", candidate.getBanderaNumbers());
        contentValues.put("Candidate_Preferential_Votes", candidate.getPreferentialVotes());
        contentValues.put("candidate_cross_votes", candidate.getCrossVote());
        database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + candidateEleID + "'", null);
    }

    //----------------------------------------------------------------------------------------------
    // CHECKLIST ACTIVITY:
    public LinkedHashMap<Integer, PresidenteStaff> getCheckListItemsFromCatalog(int isSignature) {
        LinkedHashMap<Integer, PresidenteStaff> signaturesSeals = new LinkedHashMap<>();
        String query = "select Item, Description from ActaQualityCatalog where isSignature = ? order by OrderId";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(isSignature)});
        if (cursor.moveToFirst()) {
            do {
                PresidenteStaff item = new PresidenteStaff(cursor.getString(0), cursor.getString(1));
                Log.e("ITEM FROM CATALOG", item.getDescription());
                signaturesSeals.put(item.getStaffID().hashCode(), item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return signaturesSeals;

    }

    public void saveCheckListItems(LinkedHashMap<String, String> signaturesMap) {
        for (Map.Entry<String, String> entry : signaturesMap.entrySet()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("Item", entry.getKey());
            contentValues.put("Checked", entry.getValue());
            database.insert(ACTA_QUALITY_REPORT, null, contentValues);
        }
    }

    public void deleteCheckListItems() {
        database.delete(ACTA_QUALITY_REPORT, null, null);
    }

    //----------------------------------------------------------------------------------------------
    //MESA/ JUNTA MEMBERS ACTIVITY:
    //Members:
    public ArrayList<User> getMERMembers() {
        ArrayList<User> members = new ArrayList<>();
        Cursor cursor = database.query(MERMembers,
                new String[]{USERID, USERNAME, SIGNIN, TITLE, CONFIRMED, PROPRIETARIO}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(USERID));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
                String ispresent = cursor.getString(cursor.getColumnIndexOrThrow(SIGNIN));
                String userTitle = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                String memberUpdate = cursor.getString(cursor.getColumnIndexOrThrow(PROPRIETARIO));
                User member = new User();
                member.newMemberFromDB(userId, userName, userTitle, ispresent, "Yes", memberUpdate);
                Log.e("DATABASEHELPER", member.toString());
                members.add(member);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }

    public ArrayList<User> getMERMemberswParty() {
        ArrayList<User> members = new ArrayList<>();
        Cursor cursor = database.query(MERMembers,
                new String[]{USERID, USERNAME, SIGNIN, TITLE, CONFIRMED, PROPRIETARIO}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(USERID));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
                String ispresent = cursor.getString(cursor.getColumnIndexOrThrow(SIGNIN));
                String userTitle = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                String memberUpdate = cursor.getString(cursor.getColumnIndexOrThrow(PROPRIETARIO));
                String userParty = cursor.getString(cursor.getColumnIndexOrThrow(PARTY));
                User member = new User();
                member.newMemberFromDBwParty(userId, userName, userTitle, ispresent, "Yes", memberUpdate, userParty);
                Log.e("DATABASEHELPER", member.toString());
                members.add(member);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }

    public ArrayList<User> getMesaMembers(String proprietario, String jrv) {
        ArrayList<User> members = new ArrayList<>();
        String selection = PROPRIETARIO + "= ? AND JRV =?";

        Cursor cursor = database.query(MERMembers,
                new String[]{USERID, USERNAME, SIGNIN, TITLE, CONFIRMED, PROPRIETARIO}, selection, new String[]{proprietario , jrv}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(USERID));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
                String ispresent = cursor.getString(cursor.getColumnIndexOrThrow(SIGNIN));
                String userTitle = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                if(userTitle.equals("VC-Official")) continue;
                User member = new User();
                member.newMemberFromDB(userId, userName, userTitle, ispresent, "Yes", proprietario);
                Log.e("DATABASEHELPER", member.toString());
                members.add(member);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }

    public ArrayList<User> getMesaMemberswParty(String proprietario, String jrv) {
        ArrayList<User> members = new ArrayList<>();
        String selection = PROPRIETARIO + "= ? AND JRV =?";

        Cursor cursor = database.query(MERMembers,
                new String[]{USERID, USERNAME, SIGNIN, TITLE, CONFIRMED, PROPRIETARIO, PARTY}, selection, new String[]{proprietario , jrv}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(USERID));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
                String ispresent = cursor.getString(cursor.getColumnIndexOrThrow(SIGNIN));
                String userTitle = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                String userParty = cursor.getString(cursor.getColumnIndexOrThrow(PARTY));
                if(userTitle.equals("VC-Official")) continue;
                User member = new User();
                member.newMemberFromDBwParty(userId, userName, userTitle, ispresent, "Yes", proprietario, userParty);
                Log.e("DATABASEHELPER", member.toString());
                members.add(member);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }

    public ArrayList<User> getVCOfficial(String proprietario, String jrv) {
        ArrayList<User> members = new ArrayList<>();
        String selection = PROPRIETARIO + "= ? AND JRV =?";

        Cursor cursor = database.query(MERMembers,
                new String[]{USERID, USERNAME, SIGNIN, TITLE, CONFIRMED, PROPRIETARIO}, selection, new String[]{proprietario , jrv}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(USERID));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
                String ispresent = cursor.getString(cursor.getColumnIndexOrThrow(SIGNIN));
                String userTitle = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                if(!userTitle.equals("VC-Official")) continue;
                User member = new User();
                member.newMemberFromDB(userId, userName, userTitle, ispresent, "Yes", proprietario);
                Log.e("DATABASEHELPER", member.toString());
                members.add(member);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }

    public ArrayList<User> getAttendeeMembers() {
        ArrayList<User> members = new ArrayList<>();
        Log.e("DATABASEHELPER ", "getting members who attended only.... :");
        String selection = SIGNIN + "= ?";
        Cursor cursor = database.query(MERMembers,
                new String[]{USERID, USERNAME, SIGNIN, TITLE, CONFIRMED, PROPRIETARIO}, selection, new String[]{"Yes"}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(USERID));
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
                String ispresent = cursor.getString(cursor.getColumnIndexOrThrow(SIGNIN));
                String userTitle = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                String proprietario = cursor.getString(cursor.getColumnIndexOrThrow(PROPRIETARIO));
                //String confirmation = cursor.getString(cursor.getColumnIndexOrThrow(CONFIRMED));
                User member = new User();
                member.newMemberFromDB(userId, userName, userTitle, ispresent, "Yes", proprietario);
                Log.e("DATABASEHELPER", member.toString());
                members.add(member);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }

    public void insertMERMembers(String userId, String member, String title, String ispresent,
                                 String isconfirmed, String proprietario, String jrv) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERID, userId);
        contentValues.put(USERNAME, member);
        contentValues.put(SIGNIN, ispresent);
        contentValues.put(CONFIRMED, isconfirmed);
        contentValues.put(TITLE, title);
        contentValues.put(PROPRIETARIO, proprietario);
        contentValues.put(JRV, jrv);
        //contentValues.put(ORDER, cargoOrder);
        database.insert(MERMembers, null, contentValues);
    }

    public void insertMERMemberswParty(String userId, String member, String title, String ispresent,
                                 String isconfirmed, String proprietario, String jrv, String p) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERID, userId);
        contentValues.put(USERNAME, member);
        contentValues.put(SIGNIN, ispresent);
        contentValues.put(CONFIRMED, isconfirmed);
        contentValues.put(TITLE, title);
        contentValues.put(PROPRIETARIO, proprietario);
        contentValues.put(JRV, jrv);
        contentValues.put(PARTY, p);
        //contentValues.put(ORDER, cargoOrder);
        database.insert(MERMembers, null, contentValues);
    }

    public void updateMERMembers(String userId, String member, String title, String ispresent,
                                 String isconfirmed, String cargoOrder, String proprietario) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERID, userId);
        contentValues.put(USERNAME, member);
        contentValues.put(TITLE, title);
        contentValues.put(SIGNIN, ispresent);
        contentValues.put(CONFIRMED, isconfirmed);
        contentValues.put(PROPRIETARIO, proprietario);
        //contentValues.put(ORDER, cargoOrder);
        database.update(MERMembers, contentValues, TITLE + "= '" + title + "' AND " + PROPRIETARIO + "= '" + proprietario + "'", null);
    }

    public void deleteMERMembers() {
        try {
            database.query(MERMembers, null, null, null, null, null, null); //Check if table exists
            database.delete(MERMembers, null, null);
        } catch (SQLiteException e) {
            Log.d(e.getMessage(), MERMembers + " Doesn't exist");
        }
    }
    // ACTA Attendees
    public void deleteActaAttendees(){
        try {
            database.query(Attendees, null, null, null, null, null, null); //Check if table exists
            database.delete(Attendees, null, null);
        } catch (SQLiteException e) {
            Log.d(e.getMessage(), MERMembers + " Doesn't exist");
        }

    }

    public void insertActaAttendees(User member){
        Log.e("ROLLCALL Acta: ",member.getDUI());
        ContentValues cv = new ContentValues();
//        cv.put("MER",member.getCargoOrder());
        cv.put("MRE","0"); //member.getJRV());
        cv.put("usr_id",member.getDUI());
        cv.put("usr_title",member.getTitle());
        cv.put("present",member.getIspresent());
        database.insert(Attendees,null,cv);
    }

    public void insertActaAttendeesInOrder(User member,int order){
        Log.e("ROLLCALL Acta: ",member.getDUI());
        ContentValues cv = new ContentValues();
        cv.put("MER",order);
//        cv.put("MRE","0"); //member.getJRV());
        cv.put("usr_id",member.getDUI());
        cv.put("usr_title",member.getTitle());
        cv.put("present",member.getIspresent());
        database.insert(Attendees,null,cv);
    }

    public ArrayList<User> getActaAttendees(){
        ArrayList<User> members = new ArrayList<>();
        Cursor cursor = database.query(Attendees,
                new String[]{"MRE","usr_id","usr_title","present"},null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String userId = cursor.getString(cursor.getColumnIndexOrThrow("usr_id"));
                String userName = "";//cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
                String ispresent = cursor.getString(cursor.getColumnIndexOrThrow("present"));
                String userTitle = cursor.getString(cursor.getColumnIndexOrThrow("usr_title"));
                String proprietario = "";//cursor.getString(cursor.getColumnIndexOrThrow(PROPRIETARIO));
                //String confirmation = cursor.getString(cursor.getColumnIndexOrThrow(CONFIRMED));
                User member = new User();
                member.newMemberFromDB(userId, userName, userTitle, ispresent, "Yes", proprietario);
                Log.e("DATABASEHELPER", member.toString());
                members.add(member);

            }while (cursor.moveToNext());
        }
        cursor.close();
        return members;
    }

    public int getActaAttendeesNumber(){
        ArrayList<User> members = new ArrayList<>();
        String selection = "present =?";
        Cursor cursor = database.query(Attendees,
                new String[]{"MRE","usr_id","usr_title","present"},selection,new String[]{"Yes"},null,null,null);
        if(cursor.moveToFirst()){
            do{
                String userId = cursor.getString(cursor.getColumnIndexOrThrow("usr_id"));
                String userName = "";//cursor.getString(cursor.getColumnIndexOrThrow(USERNAME));
                String ispresent = cursor.getString(cursor.getColumnIndexOrThrow("present"));
                String userTitle = cursor.getString(cursor.getColumnIndexOrThrow("usr_title"));
                String proprietario = "";//cursor.getString(cursor.getColumnIndexOrThrow(PROPRIETARIO));
                User member = new User();
                member.newMemberFromDB(userId, userName, userTitle, ispresent, "Yes", proprietario);
                members.add(member);

            }while (cursor.moveToNext());
        }
        cursor.close();
        Log.e("HOW MANY MEMBERS",""+String.valueOf(members.size()));
        return  members.size();

    }

    public LinkedHashMap<Integer, PresidenteStaff> getCheckListItemsFromAttendees() {
        LinkedHashMap<Integer, PresidenteStaff> signaturesSeals = new LinkedHashMap<>();
        Cursor cursor = database.query(Attendees, new String[]{"usr_title"},null,null,null,null,"MRE");
//        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(isSignature)});
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(0);
                PresidenteStaff item = new PresidenteStaff(title, title.toUpperCase());
                Log.e("ITEM FROM CATALOG", item.getDescription());
                signaturesSeals.put(item.getStaffID().hashCode(), item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return signaturesSeals;
    }


    //----------------------------------------------------------------------------------------------
    public String[] getConceptosAndParties(String event_locality_id) {
        String[] queries;
        if(Consts.LOCALE.contains("ELSA")){
            queries = getRawQueriesES();
        }else queries = getRawQueriesHN();

        String sqlQueryOne = queries[0];
        String sqlQueryTwo = queries[1];
        String sqlQueryThree = queries[2];

        Cursor crs = database.rawQuery(sqlQueryOne, null);

        String[] first = new String[crs.getCount()];
        int i = 0;
        while (crs.moveToNext()) {
            String uname = crs.getString(crs.getColumnIndex("concept_text"));
            first[i] = uname;
            i++;
        }

        crs = database
                .rawQuery(sqlQueryTwo, new String[]{event_locality_id});
        String[] second = new String[crs.getCount()];
        int y = 0;
        while (crs.moveToNext()) {
            String uname = crs.getString(crs.getColumnIndex("Party"));
            second[y] = uname;
            y++;
        }

        crs = database.rawQuery(sqlQueryThree, null);

        String[] third = new String[crs.getCount()];
        int z = 0;
        while (crs.moveToNext()) {
            String uname = crs.getString(crs.getColumnIndex("concept_text"));
            third[z] = uname;
            z++;
        }

        String[] activeConceptos = CollectionUtils.join(first, second, third);

        return activeConceptos;
    }


    public List<PreferentialCandidateVotes> getPreferentialElectionCandidateVotesForThisParty(
            String party_preferential_election_id) {
        List<PreferentialCandidateVotes> prefCandVotesList = new ArrayList<PreferentialCandidateVotes>();

        String[] tableColumns = new String[]{"JRV",
                "Party_Preferential_Election_ID",
                "Candidate_Preferential_Election_ID", "Candidate_Votes", "Candidate_Bandera_Votes", "Candidate_Preferential_Votes"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_CANDIDATE_VOTES,
                tableColumns, "Party_Preferential_Election_ID=?",
                new String[]{party_preferential_election_id}, null, null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PreferentialCandidateVotes prefCandVotes = new PreferentialCandidateVotes();

            prefCandVotes.setJrv(cursor.getInt(0));
            prefCandVotes
                    .setParty_preferential_election_id(cursor.getString(1));
            prefCandVotes.setCandidate_preferential_election_id(cursor
                    .getString(2));
            prefCandVotes.setCandidate_votes(cursor.getFloat(3));
            prefCandVotes.setCandidate_bandera_votes(cursor.getFloat(4));
            prefCandVotes.setCandidate_preferential_votes(cursor.getFloat(5));
            prefCandVotesList.add(prefCandVotes);
            cursor.moveToNext();
        }
        cursor.close();

        return prefCandVotesList;

    }

    public List<PreferentialCandidateVotes> getPreferentialElectionCandidateVotes() {
        List<PreferentialCandidateVotes> prefCandVotesList = new ArrayList<PreferentialCandidateVotes>();

        String[] tableColumns = new String[]{"JRV",
                "Party_Preferential_Election_ID",
                "Candidate_Preferential_Election_ID", "Candidate_Votes", "Candidate_Bandera_Votes", "Candidate_Preferential_Votes", "Preferential_Election_ID", "candidate_cross_votes"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_CANDIDATE_VOTES,
                tableColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PreferentialCandidateVotes prefCandVotes = new PreferentialCandidateVotes();
            prefCandVotes.setJrv(cursor.getInt(0));
            prefCandVotes
                    .setParty_preferential_election_id(cursor.getString(1));
            prefCandVotes.setCandidate_preferential_election_id(cursor
                    .getString(2));
            prefCandVotes.setCandidate_votes(cursor.getFloat(3));
            prefCandVotes.setCandidate_preferential_votes(cursor.getFloat(5));
            prefCandVotes.setCandidate_bandera_votes(cursor.getFloat(4));
            prefCandVotes.setPreferential_election_id(cursor.getString(6));
            prefCandVotes.setCandidate_cross_votes(cursor.getFloat(7));
            prefCandVotesList.add(prefCandVotes);
            cursor.moveToNext();
        }
        cursor.close();

        return prefCandVotesList;

    }

    // CARLOS: APPLOG TABLE
    // 2014-09-18
    public void insertAppLog(String jrv, String electionId, String dui,
                             String screenName, String typeDescription, String originalValue,
                             String finalValue, String datetime) {

        ContentValues cont = new ContentValues();
        cont.put("JRV", jrv);
        cont.put("ELECTIONID", electionId);
        cont.put("DUI", dui);
        cont.put("SCREENNAME", screenName);
        cont.put("TYPEDESCRIPTION", typeDescription);
        cont.put("ORIGINALVALUE", originalValue);
        cont.put("FINALVALUE", finalValue);
        cont.put("DATETIME", datetime);

        database.insert(APPLOG, null, cont);
    }

    public void deleteAllAppLog() {
        database.delete(APPLOG, null, null);
    }

    public void insertPreferentialCandidateVote(Integer jrv, String string,
                                                String string2, float votesNumber, float banderaVotes, float prefVotes, String election_id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("JRV", jrv);
        contentValues.put("Party_Preferential_Election_ID", string);
        contentValues.put("Candidate_Preferential_Election_ID", string2);
        contentValues.put("Candidate_Votes", votesNumber);
        contentValues.put("Candidate_Bandera_Votes", banderaVotes);
        contentValues.put("Candidate_Preferential_Votes", prefVotes);
        contentValues.put("Preferential_Election_ID", election_id);

        database.insert(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, null,
                contentValues);

    }

    public void updatePreferentialCandidateVote(Integer jrv, String string, String string2,
                                                float votesNumber, float banderaVotes, float prefVotes) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("JRV", jrv);
        contentValues.put("Party_Preferential_Election_ID", string);
        contentValues.put("Candidate_Preferential_Election_ID", string2);
        contentValues.put("Candidate_Votes", votesNumber);
        contentValues.put("Candidate_Bandera_Votes", banderaVotes);
        contentValues.put("Candidate_Preferential_Votes", prefVotes);
        database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + string2 + "'", null);
    }

    public void updateCandidateFinalVote(Candidate candidate, String jrv) {
        String candidateEleID = candidate.getCandidatePreferentialElectionID();
        ContentValues contentValues = new ContentValues();
        contentValues.put("JRV", jrv);
        contentValues.put("Party_Preferential_Election_ID", candidate.getPartyPreferentialElectionID());
        contentValues.put("Candidate_Preferential_Election_ID", candidateEleID);
        contentValues.put("Candidate_Votes", candidate.getVotesNumber());
        contentValues.put("Candidate_Bandera_Votes", candidate.getBanderaNumbers());
        contentValues.put("Candidate_Preferential_Votes", candidate.getPreferentialVotes());
        contentValues.put("candidate_cross_votes", candidate.getCrossVote());
        database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + candidateEleID + "'", null);
    }

    public int updateCandidateFinalCrossVote(Candidate candidate) {
        String candidateEleID = candidate.getCandidatePreferentialElectionID();
        ContentValues contentValues = new ContentValues();
        contentValues.put("candidate_cross_votes", candidate.getCrossVote());
        int x = database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + candidateEleID + "'", null);
        return x;
    }

    public int updateCandidateBanderaMarks(Candidate candidate) {
        String candidateEleID = candidate.getCandidatePreferentialElectionID();
        ContentValues contentValues = new ContentValues();
        contentValues.put("candidate_bandera_marks", candidate.getBanderaMarks());
        int x = database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + candidateEleID + "'", null);
        return x;
    }

    public int updateCandidatePreferentialMarks(Candidate candidate) {
        String candidateEleID = candidate.getCandidatePreferentialElectionID();
        ContentValues contentValues = new ContentValues();
        contentValues.put("candidate_preferntial_marks", candidate.getPreferentialMarks());
        int x = database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + candidateEleID + "'", null);
        return x;
    }

    public int updateCandidateCrossVoteMarks(Candidate candidate) {
        String candidateEleID = candidate.getCandidatePreferentialElectionID();
        ContentValues contentValues = new ContentValues();
        contentValues.put("candidate_cross_vote_marks", candidate.getMarcas());
        int x = database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + candidateEleID + "'", null);
        return x;
    }

    public int updateCandidateFinalTotalMarks(Candidate candidate) {
        String candidateEleID = candidate.getCandidatePreferentialElectionID();
        ContentValues contentValues = new ContentValues();
        contentValues.put("candidate_total_marks", candidate.getMarksQty());
        int x = database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + candidateEleID + "'", null);
        return x;
    }

    public int updateCandidateCrossVoteandMarks(Candidate candidate) {
        String candidateEleID = candidate.getCandidatePreferentialElectionID();
        ContentValues contentValues = new ContentValues();
        contentValues.put("candidate_cross_vote_marks", candidate.getMarcas());
        contentValues.put("candidate_cross_votes", candidate.getCrossVote());
        int x = database.update(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, contentValues
                , "Candidate_Preferential_Election_ID='" + candidateEleID + "'", null);
        return x;
    }

    public void deleteAllPreferentialCandidateVotes() {
        database.delete(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, null, null);
    }

    public void deleteAllPreferentialCandidateVotes(String id) {
        database.delete(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, "Party_Preferential_Election_ID=?", new String[]{id});
    }

    public void resetCandidateVotesbyParty(String party_preferential_id) {
        database.delete(PREFERENTIAL_ELECTION_CANDIDATE_VOTES, party_preferential_id, null);
    }

    public long insertPartiesPreferentialVotes(Party party, String jrvString) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("JRV", jrvString);
        contentValues.put("Party_Preferential_Election_ID",
                party.getParty_preferential_election_id());
        contentValues.put("Preferential_Election_ID",
                party.getPref_election_id());
        contentValues.put("Party_Votes", party.getParty_votes());
        contentValues.put("party_boletas", party.getParty_votes());


        return database.insert(PREFERENTIAL_ELECTION_PARTY_VOTES, null, contentValues);

    }

    public int updateNumberOfChangeBoletasAndPartyBoletasToParty(
            String addedBoletas, String partyBoletas, String partyPrefElectionId) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("change_boletas", addedBoletas);
        contentValues.put("party_boletas", partyBoletas);
        contentValues.put("Party_Votes", partyBoletas);

        int x = database.update(PREFERENTIAL_ELECTION_PARTY_VOTES,
                contentValues, "Party_Preferential_Election_ID='"
                        + partyPrefElectionId + "'", null);
        return x;
    }

    public void updatePartyTotal(float votes, String partyID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("Party_Votes", votes);
        database.update(PREFERENTIAL_ELECTION_PARTY_VOTES,
                contentValues, "Party_Preferential_Election_ID='"
                        + partyID + "'", null);
    }

    public void updateNumberOfPartyBoletasToParty(String partyBoletas,
                                                  String partyPrefElectionId) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("party_boletas", partyBoletas);

        database.update(PREFERENTIAL_ELECTION_PARTY_VOTES, contentValues,
                "Party_Preferential_Election_ID='" + partyPrefElectionId + "'",
                null);

    }

    public List<PreferentialPartyVotes> getPartiesPreferentialVotes() {
        List<PreferentialPartyVotes> prefCandVotesList = new ArrayList<PreferentialPartyVotes>();

        String[] tableColumns = new String[]{"JRV",
                "Party_Preferential_Election_ID", "Preferential_Election_ID",
                "Party_Votes", "change_boletas", "party_boletas"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_PARTY_VOTES,
                tableColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PreferentialPartyVotes prefCandVotes = new PreferentialPartyVotes();

            prefCandVotes.setJrv(cursor.getInt(0));
            prefCandVotes
                    .setParty_preferential_election_id(cursor.getString(1));
            prefCandVotes.setPreferential_election_id(cursor.getString(2));
            prefCandVotes.setParty_votes(cursor.getInt(3));
            prefCandVotes.setChange_boletas(cursor.getString(4));
            prefCandVotes.setParty_boletas(cursor.getString(5));
            prefCandVotesList.add(prefCandVotes);
            cursor.moveToNext();
        }
        cursor.close();

        return prefCandVotesList;

    }

    public void deletePreferentialVotoBanderas() {
        database.delete(PREFERENTIAL_ELECTION_BANDERA_VOTES, null, null);
    }

    public void deletePreferentialVotoBanderas(String id) {
        database.delete(PREFERENTIAL_ELECTION_BANDERA_VOTES, "party_preferential_election_id=?", new String[]{id});
    }

    public void deletePartiesPreferentialVotes() {
        database.delete(PREFERENTIAL_ELECTION_PARTY_VOTES, null, null);
    }
    public void insertConceptsCountPreferential(
            LinkedHashMap<String, String> escrudataMap) {
        if(Consts.LOCALE.contains("ELSA")){
            insertConceptsCountPreferentialES(escrudataMap);
        }else{
            insertConceptsCountPreferentialHN(escrudataMap);
        }

    }


    public void insertBanderaVotes(PreferentialVotoBanderas votoBandera,
                                   String jrvString) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("jrv", jrvString);
        contentValues.put("preferential_election_id",
                votoBandera.getPreferential_election_id());
        contentValues.put("bandera_preferential_election_id",
                votoBandera.getBandera_preferential_election_id());
        contentValues.put("party_preferential_election_id",
                votoBandera.getParty_preferential_election_id());
        contentValues.put("party_votes", votoBandera.getParty_votes());
        contentValues.put("Party_Preferential_Votes", votoBandera.getParty_preferential_votes());
        contentValues.put("party_cross_votes", votoBandera.getParty_cross_votes());

        database.insert(PREFERENTIAL_ELECTION_BANDERA_VOTES, null,
                contentValues);

    }

    public void updateBanderaVotes(PreferentialVotoBanderas votoBandera,
                                   String jrvString) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("jrv", jrvString);
        contentValues.put("preferential_election_id",
                votoBandera.getPreferential_election_id());
        contentValues.put("bandera_preferential_election_id",
                votoBandera.getBandera_preferential_election_id());
        contentValues.put("party_preferential_election_id",
                votoBandera.getParty_preferential_election_id());
        contentValues.put("party_votes", votoBandera.getParty_votes());
        contentValues.put("Party_Preferential_Votes", votoBandera.getParty_preferential_votes());
        database.update(PREFERENTIAL_ELECTION_BANDERA_VOTES, contentValues,
                "party_preferential_election_id='" + String.valueOf(
                        votoBandera.getParty_preferential_election_id()) + "'", null);
    }

    public void updatePartyCrossVotes(float votes, String preferentialID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("party_cross_votes", votes);
        database.update(PREFERENTIAL_ELECTION_BANDERA_VOTES, contentValues,
                "party_preferential_election_id='" + preferentialID + "'", null);
    }

    public void updatePartyVotes(PreferentialVotoBanderas votoBandera, String jrvString){
        ContentValues contentValues = new ContentValues();
//        contentValues.put("jrv", jrvString);
        contentValues.put("preferential_election_id", votoBandera.getPreferential_election_id());
//        contentValues.put("bandera_preferential_election_id", votoBandera.getBandera_preferential_election_id());
        contentValues.put("party_preferential_election_id", votoBandera.getParty_preferential_election_id());

        contentValues.put("party_votes", votoBandera.getParty_votes());
        contentValues.put("Party_Preferential_Votes", votoBandera.getParty_preferential_votes());
        contentValues.put("party_cross_votes",votoBandera.getParty_cross_votes());

        database.update(PREFERENTIAL_ELECTION_BANDERA_VOTES, contentValues,
                "party_preferential_election_id='" + String.valueOf(
                        votoBandera.getParty_preferential_election_id()) + "'", null);

    }


    public List<PreferentialVotoBanderas> getBanderaVotesPreferential() {
        List<PreferentialVotoBanderas> prefCandVotesList = new ArrayList<PreferentialVotoBanderas>();

        String[] tableColumns = new String[]{"jrv",
                "preferential_election_id", "bandera_preferential_election_id",
                "party_preferential_election_id", "party_votes",
                "party_boletas", "Party_Preferential_Votes", "party_cross_votes"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_BANDERA_VOTES,
                tableColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PreferentialVotoBanderas prefCandVotes = new PreferentialVotoBanderas();

            prefCandVotes.setJrv(cursor.getString(0));

            // CARLOS: Patch to pass the 1002 that correspond for Assembly, I
            // need to pass this value dynamically later
            prefCandVotes.setPreferential_election_id(cursor.getString(1));
            prefCandVotes.setBandera_preferential_election_id(cursor.getString(2));
            prefCandVotes.setParty_preferential_election_id(cursor.getString(3));
            prefCandVotes.setParty_votes(cursor.getFloat(4));
            String party_boletas = cursor.getString(5);
            prefCandVotes.setParty_boletas(cursor.getString(5));
            prefCandVotes.setParty_preferential_votes(cursor.getFloat(6));
            prefCandVotes.setParty_cross_votes(cursor.getFloat(7));
            prefCandVotesList.add(prefCandVotes);
            cursor.moveToNext();
        }
        cursor.close();

        return prefCandVotesList;
    }

    public PreferentialVotoBanderas getBanderaVotesPreferential(String partyID) {
        PreferentialVotoBanderas prefCandVotesList = new PreferentialVotoBanderas();
        String[] tableColumns = new String[]{"jrv",
                "preferential_election_id", "bandera_preferential_election_id",
                "party_preferential_election_id", "party_votes",
                "party_boletas", "Party_Preferential_Votes", "party_cross_votes"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_BANDERA_VOTES,
                tableColumns, "party_preferential_election_id=?", new String[]{partyID}, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            PreferentialVotoBanderas prefCandVotes = new PreferentialVotoBanderas();

            prefCandVotes.setJrv(cursor.getString(0));
            // CARLOS: Patch to pass the 1002 that correspond for Assembly, I
            // need to pass this value dynamically later
            prefCandVotes.setPreferential_election_id(cursor.getString(1));
            prefCandVotes.setBandera_preferential_election_id(cursor.getString(2));
            prefCandVotes.setParty_preferential_election_id(cursor.getString(3));
            prefCandVotes.setParty_votes(cursor.getFloat(4));
            String party_boletas = cursor.getString(5);
            prefCandVotes.setParty_boletas(cursor.getString(5));
            prefCandVotes.setParty_preferential_votes(cursor.getFloat(6));
            prefCandVotes.setParty_cross_votes(cursor.getFloat(7));
            prefCandVotesList = prefCandVotes;

            cursor.moveToNext();
        }
        cursor.close();

        return prefCandVotesList;
    }

    public Candidate getCurrentBanderaVotes(
            String party_preferential_election_id) {
        String[] tableColumns = new String[]{"jrv",
                "preferential_election_id", "bandera_preferential_election_id",
                "party_votes", "Party_Preferential_Votes"};

        Cursor cursor = database.query(PREFERENTIAL_ELECTION_BANDERA_VOTES,
                tableColumns, "party_preferential_election_id=?",
                new String[]{party_preferential_election_id}, null, null,
                null, null);

        Candidate partidoCandidate = new Candidate();
        while (cursor.moveToNext()) {
            partidoCandidate.setPartyPreferentialElectionID(party_preferential_election_id);
            partidoCandidate.setVotesNumber(cursor.getFloat(3));
            partidoCandidate.setPreferentialVotes(cursor.getFloat(4));
        }
        cursor.close();

        return partidoCandidate;

    }

    public LinkedHashMap<String, String> getConceptsCountPreferential(){
        if(Consts.LOCALE.contains("ELSA")){
            return getConceptsCountPreferentialES();
        }else
            return getConceptsCountPreferentialHN();
    }

    //---------------------------- EL SALVADOR DB Methods -----------------------------------------

    private LinkedHashMap<String, String> getConceptsCountPreferentialES() {
        LinkedHashMap<String, String> conceptsValues = new LinkedHashMap<>();

        String[] tableColumns = new String[]{"JRV",
                "Preferential_Election_ID", "Sobrantes", "Inutilizadas",
                "Impugnados", "Nulos", "Abstenciones", "Escrutadas",
                "Faltantes", "Entregadas", "Papeletas_Recibidas"};

        Cursor cursor = database.query(
                PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES, tableColumns, null,
                null, null, null, null);

        if (cursor.moveToFirst()) {
            do {

//                conceptsValues.put("JRV", cursor.getString(0));
                conceptsValues.put("PREFERENTIAL ELECTION ID", cursor.getString(1));
                conceptsValues.put("SOBRANTES", cursor.getString(2));
                conceptsValues.put("INUTILIZADAS", cursor.getString(3));
                conceptsValues.put("IMPUGNADOS", cursor.getString(4));
                conceptsValues.put("NULOS", cursor.getString(5));
                conceptsValues.put("ABSTENCIONES", cursor.getString(6));
                conceptsValues.put("TOTAL PAPELETAS ESCRUTADAS", cursor.getString(7));
                conceptsValues.put("PAPELETAS FALTANTES", cursor.getString(8));
                conceptsValues.put("PAPELETAS ENTREGADAS", cursor.getString(9));
                conceptsValues.put("PAPELETAS RECIBIDAS", cursor.getString(10));

            } while (cursor.moveToNext());

        }
        cursor.close();
        return conceptsValues;

    }

    private void insertConceptsCountPreferentialES(
            LinkedHashMap<String, String> escrudataMap) {
        Log.e("DATABASEADAPTER: ", escrudataMap.toString());

        ContentValues contentValues = new ContentValues();
        switch (context.getResources().getString(R.string.electionType)) {
            case "MAYOR":

                break;
            case "ASAMBLEA":
                contentValues.put("JRV", escrudataMap.get("JRV"));
                contentValues.put("Preferential_Election_ID",
                        escrudataMap.get("PREFERENTIAL ELECTION ID"));

                break;
            case "PRESIDENT":
                break;
            case "PARLACEN":
                break;
            default:
                break;

        }


        contentValues.put("Sobrantes", escrudataMap.get("SOBRANTES"));
        contentValues.put("Inutilizadas", escrudataMap.get("INUTILIZADAS"));
        contentValues.put("Impugnados", escrudataMap.get("IMPUGNADOS"));
        contentValues.put("Nulos", escrudataMap.get("NULOS"));
        contentValues.put("Abstenciones", escrudataMap.get("ABSTENCIONES"));
        contentValues.put("Escrutadas",
                escrudataMap.get("ESCRUTADAS"));
        contentValues.put("Faltantes", escrudataMap.get("FALTANTES"));
        contentValues.put("Entregadas",
                escrudataMap.get("ENTREGADAS"));
        //contentValues.put("Papeletas_Recibidas",
        //		escrudataMap.get("PAPELETAS RECIBIDAS"));
        database.insert(PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES, null,
                contentValues);

    }


    private String[] getRawQueriesES(){
        String[] queries = new String[3];
        String electiontype = context.getResources().getString(R.string.electionType);
        queries[0]=  "SELECT * FROM [concepts] WHERE [ConceptID] in ('1', '2')";
        switch (electiontype) {
            case "MAYOR":
                queries[1] = "Select * from [direct_election_parties] Where CAST([Direct_Election_ID] as integer) = ? ORDER BY CAST([Party_Order] AS INTEGER)";
                queries[2] = "SELECT * FROM [concepts] WHERE [ConceptID] in ('3', '4', '5', '6', '7', '8') ORDER BY  CAST(concept_order as integer) ";
                break;
            case "ASAMBLEA":
                queries[1] = "Select * from [preferential_election_parties] Where CAST([Preferential_Election_ID] as integer) = ? ORDER BY CAST([Party_Order] AS INTEGER)";
                queries[2] = "SELECT * FROM [concepts] WHERE [ConceptID] in ('3', '4', '5', '6', '7', '8','9') ORDER BY  CAST(concept_order as integer) ";
                break;
            case "PRESIDENT":
                break;
            case "PARLACEN":
                break;
            default:
                queries[1] = "Select * from [direct_election_parties] Where CAST([Direct_Election_ID] as integer) = ? ORDER BY CAST([Party_Order] AS INTEGER)";
                queries[2] = "SELECT * FROM [concepts] WHERE [ConceptID] in ('3', '4', '5', '6', '7', '8') ORDER BY  CAST(concept_order as integer) ";
                break;
        }
        return queries;
    }

    //------------------------------ Honduras DB Methods -------------------------------------------
    private LinkedHashMap<String, String> getConceptsCountPreferentialHN() {
        LinkedHashMap<String, String> conceptsValues = new LinkedHashMap<>();

        String[] tableColumns = new String[] { "JRV",
                "Preferential_Election_ID", "Recibidas", "Sobrantes",
                "Utilizadas", "Ciudadanos", "Mer", "TotalVotantes",
                "VotosValidos", "EnBlanco", "Nulos", "GranTotal"};

        Cursor cursor = database.query(
                PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES, tableColumns, null,
                null, null, null, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count++;
                Log.e("CONCEPT ENTRIES", String.valueOf(count));

//                conceptsValues.put("JRV", cursor.getString(0));
//                conceptsValues.put("PREFERENTIAL ELECTION ID", cursor.getString(1));
                conceptsValues.put("PAPELETAS RECIBIDAS", cursor.getString(2));
                conceptsValues.put("SOBRANTES", cursor.getString(3));
                conceptsValues.put("UTILIZADAS", cursor.getString(4));
                conceptsValues.put("CIUDADANOS", cursor.getString(5));
                conceptsValues.put("MER", cursor.getString(6));
                conceptsValues.put("TOTAL VOTANTES", cursor.getString(7));
                conceptsValues.put("VOTOS VALIDOS", cursor.getString(8));
                conceptsValues.put("EN BLANCO", cursor.getString(9));
                conceptsValues.put("NULOS", cursor.getString(10));
                conceptsValues.put("GRAN TOTAL",cursor.getString(11));

            } while (cursor.moveToNext());

        }
        cursor.close();
        return conceptsValues;

    }

    private void insertConceptsCountPreferentialHN(
            LinkedHashMap<String, String> escrudataMap) {
        Log.e("DATABASEADAPTER: ", escrudataMap.toString());

        ContentValues contentValues = new ContentValues();

        contentValues.put("JRV", escrudataMap.get("JRV"));
        contentValues.put("Preferential_Election_ID", escrudataMap.get("PREFERENTIAL ELECTION ID"));
        contentValues.put("Recibidas", escrudataMap.get("PAPELETAS RECIBIDAS"));
        contentValues.put("Sobrantes", escrudataMap.get("NO UTILIZADAS / SOBRANTES"));
        contentValues.put("Utilizadas", escrudataMap.get("UTILIZADAS"));
        contentValues.put("Ciudadanos", escrudataMap.get("CIUDADANOS QUE VOTARON"));
        contentValues.put("Mer", escrudataMap.get("MER QUE VOTARON"));
        contentValues.put("TotalVotantes", escrudataMap.get("TOTAL VOTANTES"));
        contentValues.put("VotosValidos", escrudataMap.get("VOTOS VALIDOS"));
        contentValues.put("EnBlanco", escrudataMap.get("EN BLANCO"));
        contentValues.put("Nulos", escrudataMap.get("NULOS"));
        contentValues.put("GranTotal", escrudataMap.get("GRAN TOTAL"));

        database.insert(PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES, null,
                contentValues);

    }

    public void updateConceptsCount(LinkedHashMap<String,String> escrutadaMap, String jrv){
        if(Consts.LOCALE.contains("ELSA")){
            updateConceptCountSV(escrutadaMap,jrv);
        }else
            updateConceptCountHN(escrutadaMap,jrv);
    }

    private void updateConceptCountSV(LinkedHashMap<String,String> escrudataMap,String jrv){
        Log.e("DATABASEADAPTER: ", escrudataMap.toString());
        ContentValues contentValues = new ContentValues();

        contentValues.put("Sobrantes", escrudataMap.get("SOBRANTES"));
        contentValues.put("Inutilizadas", escrudataMap.get("INUTILIZADAS"));
        contentValues.put("Impugnados", escrudataMap.get("IMPUGNADOS"));
        contentValues.put("Nulos", escrudataMap.get("NULOS"));
        contentValues.put("Abstenciones", escrudataMap.get("ABSTENCIONES"));
        contentValues.put("Escrutadas",
                escrudataMap.get("ESCRUTADAS"));
        contentValues.put("Faltantes", escrudataMap.get("FALTANTES"));
        contentValues.put("Entregadas",
                escrudataMap.get("ENTREGADAS"));
        int rows = database.update(PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES,contentValues, "JRV= '"+jrv+"'", null);
        Log.e("CROSSUPDATE","rows updated: "+String.valueOf(rows)+" of JRV = "+jrv);
    }

    private void updateConceptCountHN(LinkedHashMap<String,String> escrudataMap, String jrv){
        Log.e("DATABASEADAPTER: ", escrudataMap.toString());

        ContentValues contentValues = new ContentValues();

//        contentValues.put("JRV", escrudataMap.get("JRV"));
//        contentValues.put("Preferential_Election_ID", escrudataMap.get("PREFERENTIAL ELECTION ID"));
        contentValues.put("Recibidas", escrudataMap.get("PAPELETAS RECIBIDAS"));
        contentValues.put("Sobrantes", escrudataMap.get("NO UTILIZADAS / SOBRANTES"));
        contentValues.put("Utilizadas", escrudataMap.get("UTILIZADAS"));
        contentValues.put("Ciudadanos", escrudataMap.get("CIUDADANOS QUE VOTARON"));
        contentValues.put("Mer", escrudataMap.get("MER QUE VOTARON"));
        contentValues.put("TotalVotantes", escrudataMap.get("TOTAL VOTANTES"));
        contentValues.put("VotosValidos", escrudataMap.get("VOTOS VALIDOS"));
        contentValues.put("EnBlanco", escrudataMap.get("EN BLANCO"));
        contentValues.put("Nulos", escrudataMap.get("NULOS"));
        contentValues.put("GranTotal", escrudataMap.get("GRAN TOTAL"));
        int rows = database.update(PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES,contentValues, "JRV= '"+jrv+"'", null);
        Log.e("CROSSUPDATE","rows updated: "+String.valueOf(rows)+" of JRV = "+jrv);
    }

    public void updateConceptCount(String key, String value, String jrv){
        ContentValues contentValues = new ContentValues();
        contentValues.put(key,value);
        int rows = database.update(PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES,contentValues, "JRV= '"+jrv+"'", null);
        Log.e("CROSSUPDATE","rows updated: "+String.valueOf(rows)+" of JRV = "+jrv);
    }

    public void deleteConceptCount() {
        database.delete(PREFERENTIAL_ELECTION_CONCEPTS_COUNT_VOTES, null, null);

    }

    private String[] getRawQueriesHN(){
        String[] queries = new String[3];
        String electiontype = context.getResources().getString(R.string.electionType);
        queries[0]=  "SELECT * FROM [concepts] WHERE [ConceptID] in ('1', '2')";
        switch (electiontype) {
            case Consts.MAYOR:
                queries[1] = "Select * from [direct_election_parties] Where CAST([Direct_Election_ID] as integer) = ? ORDER BY CAST([Party_Order] AS INTEGER)";
                queries[2] = "SELECT * FROM [concepts] WHERE [ConceptID] in ('3', '4', '5', '6', '7', '8','10','11') ORDER BY  CAST(concept_order as integer) ";
                break;
            case Consts.ASAMBLEA:
                //todo updatate:
                queries[1] = "Select * from [preferential_election_parties] Where CAST([Preferential_Election_ID] as integer) = ? ORDER BY CAST([Party_Order] AS INTEGER)";
                queries[2] = "SELECT * FROM [concepts] WHERE [ConceptID] in ('3', '4', '5', '6', '7', '8','9','10','11') ORDER BY  CAST(concept_order as integer) ";
                break;
            case Consts.PRESIDENT:
                queries[1]= "Select * from [direct_election_parties] Where CAST([Direct_Election_ID] as integer) = ? ORDER BY CAST([Party_Order] AS INTEGER)";
                queries[2] = "SELECT * FROM [CONCEPTS] WHERE [ConcepTID] in ('3', '4', '5', '6', '7', '8', '10', '11') ORDER BY  CAST(concept_order as integer) ";

                break;
            case Consts.PARLACEN:
                break;
            default:
                //todo: throw execption, election type should always be defined!
                queries[1] = "Select * from [direct_election_parties] Where CAST([Direct_Election_ID] as integer) = ? ORDER BY CAST([Party_Order] AS INTEGER)";
                queries[2] = "SELECT * FROM [concepts] WHERE [ConceptID] in ('3', '4', '5', '6', '7', '8') ORDER BY  CAST(concept_order as integer) ";
                break;
        }
        return queries;

    }

    public HashMap<String, String> getPartiesCandidatesIDs(String election_ID){
        String[] tableColumns = new String[] { "Party_Direct_Election_ID",
                "Candidate_Direct_Election_ID"};
        Cursor cursor = database.query(DIRECT_ELECTION_PARTIES,
                tableColumns, "Direct_Election_ID=?", new String[]{election_ID}, null, null, null);
        cursor.moveToFirst();
        HashMap<String,String> party_candidates= new HashMap<>();
        while (!cursor.isAfterLast()) {
            String party_id = cursor.getString(cursor.getColumnIndexOrThrow("Party_Direct_Election_ID"));
            String candidate_id = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_Direct_Election_ID"));
            party_candidates.put(party_id,candidate_id);
            cursor.moveToNext();
        }
        cursor.close();
        return party_candidates;
    }
    //----------------------------------------------------------------------------------------------
    public void insertPlanchaVote(String jrv,
    String pref_elec_id,
    String party_elec_id,
    String candidate_elec_id,
    float candidate_vote,
    String candidate_boleta_no) {
        database.beginTransactionNonExclusive();
        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put("jrv", jrv);
            contentValues.put("electionId", pref_elec_id);
            contentValues.put("partyId", party_elec_id);
            contentValues.put("candidateId", candidate_elec_id);
            contentValues.put("vote", candidate_vote);
            contentValues.put("ballotNumber", candidate_boleta_no);
            database.insert(CANDIDATE_PLANCHAVOTES, null, contentValues);
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }

    }

    public void insertPreferentialVote(String jrv,
                                  String pref_elec_id,
                                  String party_elec_id,
                                  String candidate_elec_id,
                                  float candidate_vote,
                                  String candidate_boleta_no) {
        database.beginTransactionNonExclusive();
        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put("jrv", jrv);
            contentValues.put("electionId", pref_elec_id);
            contentValues.put("partyId", party_elec_id);
            contentValues.put("candidateId", candidate_elec_id);
            contentValues.put("vote", candidate_vote);
            contentValues.put("ballotNumber", candidate_boleta_no);
            database.insert(CANDIDATE_PREFERENTIALVOTES, null, contentValues);
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }

    }

    public void updatePlanchaPreferentialVotes(){
        database.beginTransaction();
        String planchaUpdate = "  update preferential_election_candidate_votes " +
                " set candidate_bandera_votes = (select sum(vote) + candidate_bandera_votes from plancha_vote_temp pt where pt.candidateId = Candidate_Preferential_Election_ID) " +
                " where Candidate_Preferential_Election_ID IN (select candidateId from plancha_vote_temp pt where pt.candidateId = Candidate_Preferential_Election_ID)";
        String preferentialUpdate = "  update preferential_election_candidate_votes" +
                "  set candidate_preferential_votes = (select sum(vote) + candidate_preferential_votes from pref_vote_temp pvt where pvt.candidateId = Candidate_Preferential_Election_ID)" +
                "  where Candidate_Preferential_Election_ID IN (select candidateId from pref_vote_temp pt where pt.candidateId = Candidate_Preferential_Election_ID)";

        try{
            database.execSQL(planchaUpdate);
            database.execSQL(preferentialUpdate);
            database.setTransactionSuccessful();
        }finally {
            database.endTransaction();
        }

    }
    public void deleteTempVotes(String... tables){
        for (String tb: tables) {
            database.delete(tb, null, null);
            Log.e("deleteTempVotes", "deleted");
//            database.delete(CANDIDATE_PREFERENTIALVOTES, null, null);
//            database.delete(CANDIDATE_PLANCHAVOTES, null, null);
        }
    }

    public String[] getAllTempVotesTableNames() {
        return new String[] { CANDIDATE_PREFERENTIALVOTES, CANDIDATE_PLANCHAVOTES };
    }

    public static Drawable getAssetImage(Context context, String filename)
            throws IOException {
        AssetManager assets = context.getResources().getAssets();
        InputStream buffer = new BufferedInputStream((assets.open("drawable/"
                + filename + ".png")));
        Bitmap bitmap = BitmapFactory.decodeStream(buffer);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public void saveActaSignatures(LinkedHashMap<String, String> signaturesMap) {
        ContentValues contentValues = new ContentValues();

        contentValues.put("JRV", signaturesMap.get("JRV"));
        contentValues.put("ELECTION_ID", signaturesMap.get("ELECTION_ID"));
        contentValues.put("PROCESSIBLE", signaturesMap.get("PROCESSIBLE"));
        contentValues.put("PROVISIONAL_ACCEPT", signaturesMap.get("PROVISIONAL_ACCEPT"));
        contentValues.put("PRESIDENT", signaturesMap.get("PRESIDENT"));
        contentValues.put("SECRETARY", signaturesMap.get("SECRETARY"));
        contentValues.put("VOCAL1", signaturesMap.get("VOCAL1"));
        contentValues.put("VOCAL2", signaturesMap.get("VOCAL2"));
        contentValues.put("VOCAL3", signaturesMap.get("VOCAL3"));
        contentValues.put("STAMP", signaturesMap.get("STAMP"));
        contentValues.put("BARCODE", signaturesMap.get("DATETIME_LOG"));
        //Second Column
        contentValues.put("BARCODE", signaturesMap.get("BARCODE"));
        contentValues.put("TACHADURAS", signaturesMap.get("TACHADURAS"));
        contentValues.put("FORMATO", signaturesMap.get("FORMATO"));
        contentValues.put("FOLIOS", signaturesMap.get("FOLIOS"));

        database.insert(ACTA_SIGNATURES_TABLE, null, contentValues);

    }

    public void deleteActaSignatures() {
        database.delete(ACTA_SIGNATURES_TABLE, null, null);

    }

    public boolean isOpen() {
        return database.isOpen();
    }

    // * * * * * * CROSS VOTE * * * * * * * * * * * * * * * * * * * *
    public void updateCandidateCrossVote(Integer jrv,
                                         String pref_elec_id,
                                         String party_elec_id,
                                         String candidate_elec_id,
                                         float candidate_crossvote,
                                         String candidate_boleta_no) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("JRV", jrv);
        contentValues.put("Pref_Elec_ID", pref_elec_id);
        contentValues.put("Party_Pref_Elec_ID", party_elec_id);
        contentValues.put("Candidate_Pref_Elec_ID", candidate_elec_id);
        contentValues.put("Candidate_CrossVotes", candidate_crossvote);
        contentValues.put("Candidate_Boleta_No", candidate_boleta_no);
        database.update(PREFERENTIAL_CANDIDATE_CROSSVOTES, contentValues
                , "Candidate_Pref_Elec_ID='" + candidate_elec_id + "'", null);
    }

    public void deleteAllCandidateCrossVote() {
        database.delete(PREFERENTIAL_CANDIDATE_CROSSVOTES, null, null);
    }

    public void resetCandidateCrossVoteByBoleta(String party_preferential_id) {
        database.delete(PREFERENTIAL_CANDIDATE_CROSSVOTES, party_preferential_id, null);
    }

    public void insertCandidateCrossVote(String jrv,
                                         String pref_elec_id,
                                         String party_elec_id,
                                         String candidate_elec_id,
                                         float candidate_crossvote,
                                         String candidate_boleta_no) {

//		SQLiteDatabase db = dbHelper.getWritableDatabase();
        database.beginTransactionNonExclusive();

        try {
            ContentValues contentValues = new ContentValues();

            contentValues.put("JRV", jrv);
            contentValues.put("Pref_Elec_ID", pref_elec_id);
            contentValues.put("Party_Pref_Elec_ID", party_elec_id);
            contentValues.put("Candidate_Pref_Elec_ID", candidate_elec_id);
            contentValues.put("Candidate_CrossVotes", candidate_crossvote);
            contentValues.put("Candidate_Boleta_No", candidate_boleta_no);
            database.insert(PREFERENTIAL_CANDIDATE_CROSSVOTES, null, contentValues);
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }

    }
/*	String sqlQueryTotal="SELECT Party_Pref_Elec_ID, Candidate_Pref_Elec_ID," +
            "  SUM(Candidate_CrossVotes) as partyVotes FROM " + PREFERENTIAL_CANDIDATE_CROSSVOTES +
			"GROUP BY Party_Pref_Elec_ID, Candidate_Pref_Elec_ID ";*/

    public ArrayList<PreferentialVotoBanderas> getParyCrossVotes() {
        ArrayList<PreferentialVotoBanderas> listVotos = new ArrayList<>();
        String sqlQueryTotal = "SELECT Party_Pref_Elec_ID," +
                "  SUM(Candidate_CrossVotes) as partyVotes FROM " + PREFERENTIAL_CANDIDATE_CROSSVOTES +
                " GROUP BY Party_Pref_Elec_ID ";
        Cursor cursor = database.rawQuery(sqlQueryTotal, new String[]{});
        if (cursor.moveToFirst()) {
            do {
                PreferentialVotoBanderas votoBanderas = new PreferentialVotoBanderas();
                votoBanderas.setParty_preferential_election_id(cursor.getString(cursor.getColumnIndexOrThrow("Party_Pref_Elec_ID")));
                votoBanderas.setParty_cross_votes(cursor.getFloat(cursor.getColumnIndexOrThrow("partyVotes")));
                listVotos.add(votoBanderas);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return listVotos;
    }

    public float getTotalCrossVote(String jrv) {
        float currentCV = 0;
        String sqlQuery = "SELECT " +
                " SUM(Candidate_CrossVotes) AS Candidate_CrossVotes  " +
                " FROM " + PREFERENTIAL_CANDIDATE_CROSSVOTES +
                " WHERE Jrv = ?  ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{jrv});
        if (cursor.moveToFirst()) {
            do {
                String TotalCV = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_CrossVotes"));
                currentCV = Float.parseFloat(TotalCV);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return currentCV;
    }

    public ArrayList<CrossVoteBundle> getCrossVoteBundleArrayList(String jrv) {
        ArrayList<CrossVoteBundle> list = new ArrayList<CrossVoteBundle>();
//		CrossVoteBundle list = null;

        String sqlQuery = "SELECT JRV, Pref_Elec_ID, Party_Pref_Elec_ID, Candidate_Pref_Elec_ID, Candidate_CrossVotes, Candidate_Boleta_No" +
                " FROM " + PREFERENTIAL_CANDIDATE_CROSSVOTES +
                " WHERE JRV=? " +
                " ORDER BY Party_Pref_Elec_ID";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{jrv});
        if (cursor.moveToFirst()) {
            do {

                String Jrv = jrv;  //cursor.getString(cursor
                //		.getColumnIndexOrThrow("jrv"));
                String Pref_Elec_ID = cursor
                        .getString(cursor
                                .getColumnIndexOrThrow("Pref_Elec_ID"));
                String Party_Pref_Elec_ID = cursor.getString(cursor
                        .getColumnIndexOrThrow("Party_Pref_Elec_ID"));
                String Candidate_Pref_Elec_ID = cursor.getString(cursor
                        .getColumnIndexOrThrow("Candidate_Pref_Elec_ID"));
                String Candidate_CrossVotes = cursor.getString(cursor
                        .getColumnIndexOrThrow("Candidate_CrossVotes"));
                String Candidate_Boleta_No = cursor.getString(cursor
                        .getColumnIndexOrThrow("Candidate_Boleta_No"));

                CrossVoteBundle cls = new CrossVoteBundle(
                        Jrv,
                        Pref_Elec_ID,
                        Party_Pref_Elec_ID,
                        Candidate_Pref_Elec_ID,
                        Float.valueOf(Candidate_CrossVotes),
                        Candidate_Boleta_No);

                list.add(cls);
//				list = cls;
            } while (cursor.moveToNext());

        }
        cursor.close();
        return list;
    }


    public void insertMarks(String jrv, String elecId, String candidateId,
                            String partyId, String electionType, String totalMarks) {

        database.beginTransactionNonExclusive();

        try {
            ContentValues cont = new ContentValues();
            cont.put("Jrv", jrv);
            cont.put("ElecId", elecId);
            cont.put("CandidateId", candidateId);
            cont.put("PartyId", partyId);
            cont.put("ElectionType", electionType);
            cont.put("TotalMarks", totalMarks);

            database.insert(MARKS, null, cont);
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }

    }

    public int getCandidatMarks(String candidateId, String electionType){
        int marks = 0;
        String sqlQuery = "SELECT " + "ifnull(TotalMarks,0) AS TotalMarks " + "FROM " + MARKS + " WHERE CandidateId = ? AND ElectionType = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {candidateId, electionType});
        if (cursor.moveToFirst()) {
            do {
                String TotalMarks = cursor.getString(cursor.getColumnIndexOrThrow("TotalMarks"));
                marks += Integer.parseInt(TotalMarks);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return marks;
    }

    public void deletePartyMarks(String partyID, String electionType){
//        DELETE FROM Marks WHERE PartyId = 10222 AND  ElectionType= 4
        String whereClause ="PartyId = ? AND  ElectionType = ?" ;
        String [] whereArgs = new String[] {partyID, electionType};
//      public int delete(String table, String whereClause, String[] whereArgs) {
        database.delete(MARKS, whereClause, whereArgs);
    }


    public int getPartyMark (String partyID, String markType){
        int marks = 0;
        String sqlQuery = "SELECT " + "ifnull(SUM(TotalMarks),0) AS TotalMarks " + "FROM " + MARKS + " WHERE PartyId = ? AND ElectionType = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {partyID, markType});
        if(cursor.moveToFirst()){
            do {
                String TotalMarks = cursor.getString(cursor.getColumnIndexOrThrow("TotalMarks"));
                marks = Integer.parseInt(TotalMarks);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return marks;
    }

    public int getCandMark (String candID, String markType){
        int marks = 0;
        String sqlQuery = "SELECT " + "ifnull(SUM(TotalMarks),0) AS TotalMarks " + "FROM " + MARKS + " WHERE CandidateId = ? AND ElectionType = ? ";
        Cursor cursor = database.rawQuery(sqlQuery, new String[] {candID, markType});
        if(cursor.moveToFirst()){
            do {
                String TotalMarks = cursor.getString(cursor.getColumnIndexOrThrow("TotalMarks"));
                marks = Integer.parseInt(TotalMarks);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return marks;
    }

    public int getTotalMarks(String jrv, String electype) {
        int currentTotalMarks = 0;
        String sqlQuery = "SELECT " +
                " SUM(TotalMarks) AS TotalMarks  " +
                " FROM " + MARKS + " INNER JOIN " + ELECTION_TYPES + " ON ElectionType = ElecTypeId " +
                " WHERE Jrv = ? AND ElectionType = ? ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{jrv, electype});
        if (cursor.moveToFirst()) {
            do {
                String TotalMarks = cursor.getString(cursor.getColumnIndexOrThrow("TotalMarks"));
                currentTotalMarks = Integer.parseInt(TotalMarks);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return currentTotalMarks;
    }

    public ArrayList<CandidateMarks> getAllCandidateMarksArrayListByParty(String partyElecId) {
        ArrayList<CandidateMarks> list = new ArrayList<CandidateMarks>();

        String sqlQuery = "SELECT Jrv, ElecId, CandidateId, PartyId, ElectionTypes.ElecType, " +
                " SUM(TotalMarks) AS TotalMarks  " +
                " FROM " + MARKS + " INNER JOIN " + ELECTION_TYPES + " ON ElectionType = ElecTypeId " +
                " WHERE PartyId  = ? AND ElectionType IN ('4', '6') " +
                " GROUP BY CandidateId " +
                " ORDER BY CandidateId ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{partyElecId});
        if (cursor.moveToFirst()) {
            do {
                String Jrv = cursor.getString(cursor.getColumnIndexOrThrow("Jrv"));
                String ElecId = cursor.getString(cursor.getColumnIndexOrThrow("ElecId"));
                String CandidateId = cursor.getString(cursor.getColumnIndexOrThrow("CandidateId"));
                String PartyId = cursor.getString(cursor.getColumnIndexOrThrow("PartyId"));
                String ElecType = cursor.getString(cursor.getColumnIndexOrThrow("ElecType"));
                String TotalMarks = cursor.getString(cursor.getColumnIndexOrThrow("TotalMarks"));

                CandidateMarks cm = new CandidateMarks(
                        Jrv,
                        ElecId,
                        CandidateId,
                        PartyId,
                        ElecType,
                        Integer.valueOf(TotalMarks)
                );

                list.add(cm);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    public ArrayList<CandidateMarks> getCandidateMarksArrayListByParty(String partyElecId, String electype) {
        ArrayList<CandidateMarks> list = new ArrayList<CandidateMarks>();

        String sqlQuery = "SELECT Jrv, ElecId, CandidateId, PartyId, ElectionTypes.ElecType, " +
                " SUM(TotalMarks) AS TotalMarks  " +
                " FROM " + MARKS + " INNER JOIN " + ELECTION_TYPES + " ON ElectionType = ElecTypeId " +
                " WHERE PartyId = ? AND ElectionType = ? " +
                " GROUP BY CandidateId " +
                " ORDER BY CandidateId ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{partyElecId, electype});
        if (cursor.moveToFirst()) {
            do {
                String Jrv = cursor.getString(cursor.getColumnIndexOrThrow("Jrv"));
                String ElecId = cursor.getString(cursor.getColumnIndexOrThrow("ElecId"));
                String CandidateId = cursor.getString(cursor.getColumnIndexOrThrow("CandidateId"));
                String PartyId = cursor.getString(cursor.getColumnIndexOrThrow("PartyId"));
                String ElecType = electype;
                String TotalMarks = cursor.getString(cursor.getColumnIndexOrThrow("TotalMarks"));

                CandidateMarks cm = new CandidateMarks(
                        Jrv,
                        ElecId,
                        CandidateId,
                        PartyId,
                        ElecType,
                        Integer.valueOf(TotalMarks)
                );

                list.add(cm);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    public ArrayList<CandidateMarks> getCandidateMarksArrayList(String jrv, String electype) {
        ArrayList<CandidateMarks> list = new ArrayList<CandidateMarks>();

//		String sqlQuery = "SELECT Jrv, ElecId, CandidateId, PartyId, ElectionType " +
//				" FROM " + MARKS +
//				" WHERE Jrv = ? AND ElectionType = ? ";

//		String sqlQuery = "SELECT Jrv, ElecId, CandidateId, PartyId, ElectionTypes.ElecTypeId, " +
//				" ElectionTypes.ElecType, SUM(TotalMarks) AS TotalMarks  " +
//				" FROM Marks INNER JOIN ElectionTypes ON ElectionType = ElecTypeId " +
//				" WHERE Jrv = ? AND ElectionType = ? " +
//				" GROUP BY CandidateId " +
//				" ORDER BY CandidateId ";

        String sqlQuery = "SELECT Jrv, ElecId, CandidateId, PartyId, ElectionTypes.ElecType, " +
                " SUM(TotalMarks) AS TotalMarks  " +
                " FROM " + MARKS + " INNER JOIN " + ELECTION_TYPES + " ON ElectionType = ElecTypeId " +
                " WHERE Jrv = ? AND ElectionType = ? " +
                " GROUP BY CandidateId " +
                " ORDER BY CandidateId ";

        Cursor cursor = database.rawQuery(sqlQuery, new String[]{jrv, electype});
        if (cursor.moveToFirst()) {
            do {
                String Jrv = jrv;
                String ElecId = cursor.getString(cursor.getColumnIndexOrThrow("ElecId"));
                String CandidateId = cursor.getString(cursor.getColumnIndexOrThrow("CandidateId"));
                String PartyId = cursor.getString(cursor.getColumnIndexOrThrow("PartyId"));
                String ElecType = electype;
                String TotalMarks = cursor.getString(cursor.getColumnIndexOrThrow("TotalMarks"));

                CandidateMarks cm = new CandidateMarks(
                        Jrv,
                        ElecId,
                        CandidateId,
                        PartyId,
                        ElecType,
                        Integer.valueOf(TotalMarks)
                );

                list.add(cm);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }

    public void deleteAllCandidateMarks() {
        database.delete(MARKS, null, null);
    }

    public void deleteAllCandidateMarks(String id) {
        database.delete(MARKS, "PartyId=?", new String[]{id});
    }

    public void resetCandidateMarksByJRV(String Jrv) {
        database.delete(MARKS, Jrv, null);
    }

    public void resetCandidateMarksByCandidateId(String candidateId) {
        database.delete(MARKS, candidateId, null);
    }

    //  * * * * * * *  END OF CROSS VOTES * * * * * * * * * * * * * * * * * * * * * *

    /**
     * CREATE TABLE `candidate_crossvotes` (
     * `JRV`	TEXT,
     * `Pref_Elec_ID`	TEXT,
     * `Party_Pref_Elec_ID`	TEXT,
     * `Candidate_Pref_Elec_ID`	TEXT,
     * `Candidate_CrossVotes`	FLOAT,
     * `Candidate_Boleta_No`	TEXT
     * )
     **/
    public void deleteTemp() {
        database.delete("_Candidates", null, null);
    }

    public void insertTemp(String prefId) {
        String sql2 =
                "insert into _Candidates(" +
                        "ElecId , " +
                        "PartyId , " +
                        "CandidateId ) " +
                        "select " +
                        "Preferential_Election_ID, " +
                        "Party_Preferential_Election_ID, " +
                        "Candidate_Preferential_Election_ID " +
                        "from preferential_election_candidates " +
                        "where Preferential_Election_ID = '" + prefId + "'";
        database.execSQL(sql2);
    }

    public String testTemp() {
        StringBuilder sb = new StringBuilder();
        Cursor cursor = database.query("_Candidates", new String[]{"CandidateId"}, null, null, null, null, null);
        if (cursor.moveToNext()) {
            do {
                String ls = cursor.getString(0);

                sb.append(ls);
            } while (cursor.moveToNext());
        }
        return sb.toString();
    }
    @Deprecated
    public ArrayList<CandidateMarks> getMarksArrayListToSend(String ElectionID, String jrv) {

        String sql3 = "select  u.Candidate_Preferential_Election_ID,  u.Preferential_Election_ID, u.Party_Preferential_Election_ID, " +
                "                 s3.TotalMarks as PreferentialMarks, " +
                "                 SUM(s4.TotalMarks) as CrossMarks " +
                "                 from preferential_election_candidates u " +
                "                 left outer join  Marks s3 on u.Candidate_Preferential_Election_ID  = s3. CandidateId and  s3.ElectionType = 4 " +
                "                 left outer join  Marks s4 on u.Candidate_Preferential_Election_ID = s4. CandidateId  and  s4.ElectionType = 6 " +
                "  where u.Preferential_Election_ID = '"+ElectionID+
                "'                 GROUP BY u.Candidate_Preferential_Election_ID, u.Preferential_Election_ID, u.Party_Preferential_Election_ID, s3.TotalMarks, s4.TotalMarks";

        Cursor cursor = database.rawQuery(sql3, null);
        ArrayList<CandidateMarks> list = new ArrayList<CandidateMarks>();
        if (cursor.moveToFirst()) {
            do {
                String candidateId = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_Preferential_Election_ID"));
                String partyId = cursor.getString(cursor.getColumnIndexOrThrow("Party_Preferential_Election_ID"));
                int preferentialMarks = cursor.getInt(cursor.getColumnIndexOrThrow("PreferentialMarks"));
                int crossMarks = cursor.getInt(cursor.getColumnIndexOrThrow("CrossMarks"));
                int candidateMarks = preferentialMarks + crossMarks;
                CandidateMarks cm = new CandidateMarks(jrv, candidateId, partyId, ElectionID, crossMarks,
                        preferentialMarks, candidateMarks);
                list.add(cm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
    public ArrayList<CandidateMarks> getTotalMarksArrayList(String ElectionID, String jrv) {
        database.beginTransaction();

        String commit ="commit transaction";

        String dropPref = "drop table if exists prefMarks";
        String dropPlan = "drop table if exists planMarks";
        String dropCross= "drop table if exists crosMarks";

        String sql1 ="create table crosMarks as select CandidateId," +
                " SUM(TotalMarks) as CrossMarks" +
                " from Marks " +
                " where ElectionType = 6" +
                " group by CandidateId ";
        String sql2 = "create table planMarks as select CandidateId," +
                " SUM(TotalMarks) as PlanchaMarks " +
                " from Marks " +
                " where ElectionType = 5 " +
                " group by CandidateId ";
        String sql3 = "create table prefMarks as select CandidateId, " +
                " SUM(TotalMarks) as PreferentialMarks " +
                " from Marks " +
                " where ElectionType = 4" +
                " group by CandidateId ";
        String sql4="select  u.Candidate_Preferential_Election_ID," +
                "  u.Preferential_Election_ID, " +
                "  u.Party_Preferential_Election_ID," +
                "  ifnull(prf.PreferentialMarks,0) as PreferentialMarks, " +
                "  ifnull(pln.PlanchaMarks,0) as PlanchaMarks, " +
                "  ifnull(crs.CrossMarks,0) as CrossMarks" +
                "  from preferential_election_candidates u " +
                "  left join prefMarks prf on u.Candidate_Preferential_Election_ID = prf.CandidateId" +
                "  left join planMarks pln on u.Candidate_Preferential_Election_ID = pln.CandidateId" +
                "  left join crosMarks crs on u.Candidate_Preferential_Election_ID = crs.CandidateId" +
                "  where u.Preferential_Election_ID ='"+ElectionID+"' ";



//        String sql3 = "select  u.Candidate_Preferential_Election_ID,  u.Preferential_Election_ID, u.Party_Preferential_Election_ID, " +
//                "                 SUM(s4.TotalMarks) as PreferentialMarks, " +
//                "                 SUM(s5.TotalMarks) as PlanchaMarks, " +
//                "                 SUM(s6.TotalMarks) as CrossMarks " +
//                "                 from preferential_election_candidates u " +
//                "                 left outer join  Marks s4 on u.Candidate_Preferential_Election_ID  = s4. CandidateId and  s4.ElectionType = 4 " +
//                "                 left outer join  Marks s5 on u.Candidate_Preferential_Election_ID  = s5. CandidateId and  s5.ElectionType = 5 " +
//                "                 left outer join  Marks s6 on u.Candidate_Preferential_Election_ID = s6. CandidateId  and  s6.ElectionType = 6 " +
//                "                 where u.Preferential_Election_ID = '"+ElectionID+
//                "'                 GROUP BY u.Candidate_Preferential_Election_ID, u.Preferential_Election_ID, u.Party_Preferential_Election_ID, s5.TotalMarks, s4.TotalMarks, s6.TotalMarks ";


        try{
            database.execSQL(dropPref);
            database.execSQL(dropPlan);
            database.execSQL(dropCross);
            database.execSQL(sql1);
            database.execSQL(sql2);
            database.execSQL(sql3);
            database.setTransactionSuccessful();
        }finally {
            database.endTransaction();
        }

        Cursor cursor = database.rawQuery(sql4,null);

        ArrayList<CandidateMarks> list = new ArrayList<CandidateMarks>();
        if (cursor.moveToFirst()) {
            do {
                String candidateId = cursor.getString(cursor.getColumnIndexOrThrow("Candidate_Preferential_Election_ID"));
                String partyId = cursor.getString(cursor.getColumnIndexOrThrow("Party_Preferential_Election_ID"));
                int preferentialMarks = cursor.getInt(cursor.getColumnIndexOrThrow("PreferentialMarks"));
                int crossMarks = cursor.getInt(cursor.getColumnIndexOrThrow("CrossMarks"));
                int planchaMarks = cursor.getInt(cursor.getColumnIndexOrThrow("PlanchaMarks"));
                int candidateMarks =  preferentialMarks + crossMarks + planchaMarks; //cursor.getInt(cursor.getColumnIndexOrThrow("totalMarks")); //
                CandidateMarks cm = new CandidateMarks(jrv, candidateId, partyId, ElectionID, crossMarks,
                        preferentialMarks, candidateMarks);
                cm.setCandidate_plancha_marcas(planchaMarks);
                list.add(cm);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return list;
    }
    private void dropTempTables(String commit){


    }
            /*
        public Cursor rawQuery(String sql, String[] selectionArgs) {
            return rawQueryWithFactory(null, sql, selectionArgs, null, null);
            */


    public void logELSA(String logIndex, String Dui_1, String Dui_2, String time_stamp){
        String query = "INSERT INTO logTableELSA (logIndex, Dui_1, Dui_2, Time_stamp) VALUES ("+logIndex+","+Dui_1+","+Dui_2+","+"'"+time_stamp+"'"+")";
//        String query = "INSERT INTO logTableELSA ("+logIndex+","+Dui_1+","+Dui_2+","+time_stamp+")"+" VALUES (logIndex, Dui_1, Dui_2, Time_stamp)";
        database.rawQuery(query, null);
    }

    public void logDui1(String logIndex, String Dui_1, String time_stamp){
        Log.e("DUI LOG","DUI WAS LOGGED");
        String query = "INSERT INTO logTableELSA (logIndex, Dui_1, Dui_2, Time_stamp) VALUES ('"+logIndex+"','"+Dui_1+"','"+" "+"',"+"'"+time_stamp+"'"+")";
//        String query = "INSERT INTO logTableELSA ("+logIndex+","+Dui_1+","+Dui_1+","+time_stamp+")"+" VALUES (logIndex, Dui_1, Dui_2, Time_stamp)";
        database.execSQL(query);
    }

    public void log5Duis(String logIndex, String Dui_1, String Dui_2, String Dui_3, String Dui_4, String Dui_5, String time_stamp){
        Log.e("DUI LOG","DUI WAS LOGGED");
        String query = "INSERT INTO logTableELSA (logIndex, Dui_1, Dui_2, Dui_3, Dui_4, Dui_5, Time_stamp) VALUES ('"+logIndex+"','"+Dui_1+"','"+Dui_2+"','"+Dui_3+"','"+Dui_4+"','"+Dui_5+"',"+"'"+time_stamp+"'"+")";
//        String query = "INSERT INTO logTableELSA ("+logIndex+","+Dui_1+","+Dui_1+","+time_stamp+")"+" VALUES (logIndex, Dui_1, Dui_2, Time_stamp)";
        database.execSQL(query);
    }

    //This one an update i think, UPDATE WHere logindex = logindex AND Dui1 = dui1 and timestamp = timestamp
    public void logDui2(String logIndex, String Dui_1, String Dui_2, String old_time, String new_time){
//        String query = "UPDATE logTableELSA (logIndex ,Dui_2, Time_stamp) VALUES ("+logIndex+","+Dui_2+","+new_time+") WHERE [logIndex] = "+ logIndex +" AND [Dui_1] = "+Dui_1+" AND [Time_stamp] = "+old_time+")";
        String query = "UPDATE logTableELSA SET [Dui_2] = +'"+Dui_2+"', [Time_stamp] = +'"+new_time+"' WHERE [logIndex] = '"+ logIndex +"' AND [Dui_1] = '"+Dui_1+"' AND [Time_Stamp] = '"+old_time+"'";
        database.execSQL(query);
//        String query = "UPDATE logTableELSA SET [Dui_2] = '"+Dui_2+"', [Time_stamp] = '"+new_time+"' WHERE [logIndex] = '"+ logIndex +"' AND [Dui_1] = '"+Dui_1+"' AND [Time_Stamp] = '"+old_time+"'";
//        database.rawQuery(query, null);

//        String query = "UPDATE logTableELSA SET [Dui_2] = +'"+Dui_2+"', [Time_stamp] = +'"+new_time+
//                "' WHERE [logIndex] = '"+ logIndex +"' AND [Dui_1] = '"+Dui_1+"' AND [Time_Stamp] = '"+old_time+"'";
//        database.execSQL(query);

    }


    //Elsa log, need to pass arguments of items not stored in log, JRV  ELECTION_ID  , ID?
    public List<elsaLog> getLogELSA(String jrv, String elecID){
        List<elsaLog> log = new ArrayList<elsaLog>();

        String[] tableColumns = new String[]{"logIndex", "Dui_1", "Dui_2", "Dui_3", "Dui_4", "Dui_5", "Time_stamp"};
        Cursor cursor = database.query("logTableELSA",tableColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            elsaLog logElement = new elsaLog();
            logElement.setJrv(jrv);
            logElement.setelection_id(elecID);
            logElement.setLogIndex(cursor.getString(0));
            logElement.setDui_1(cursor.getString(1));
            logElement.setDui_2(cursor.getString(2));
            logElement.setDui_3(cursor.getString(3));
            logElement.setDui_4(cursor.getString(4));
            logElement.setDui_5(cursor.getString(5));
            logElement.setTime_stamp(cursor.getString(6));
            log.add(logElement);
            cursor.moveToNext();
        }
        cursor.close();
        return log;
    }

    //THIS FUNCTION IS FOR TESTING PURPOSES ONLY!!
    public ArrayList<String> getELSAlog(){

//        String query = "SELECT  logIndex,  Dui_1, Dui_2, Time_stamp FROM logTableELSA ";
        String query = "SELECT  * FROM logTableELSA ";
        Log.e("getELSAlog : ", " XXXXXXXXXXXXXXXXXXXXXX");
        Cursor cursor = database.rawQuery(query, null);
        ArrayList<String> list = new ArrayList<String>();
        if (cursor.moveToFirst()) {
            do {
                String logIndex = cursor.getString(cursor.getColumnIndexOrThrow("logIndex"));
                String Dui_1 = cursor.getString(cursor.getColumnIndexOrThrow("Dui_1"));
                String Dui_2 = cursor.getString(cursor.getColumnIndexOrThrow("Dui_2"));
                String time_stamp = cursor.getString(cursor.getColumnIndexOrThrow("Time_stamp"));
                String log = "Index : "+logIndex+"\nDui 1 : "+Dui_1+"\nDui 2 : "+Dui_2+"\nTime stamp : "+time_stamp;
                list.add(log);
                Log.e("log = ", logIndex);
                Log.e("log = ", Dui_1);
                Log.e("log = ", Dui_2);
                Log.e("log = ", time_stamp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void reCreateLogTable(){
        String queryDrop = "DROP TABLE LogTableELSA;";
        database.execSQL(queryDrop);
        String queryCreate = "CREATE TABLE LogTableELSA(\n" +
                "logIndex STRING DEFAULT NULL,\n" +
                "Dui_1 STRING DEFAULT NULL, \n" +
                "Dui_2 STRING DEFAULT NULL,\n" +
                "Dui_3 STRING DEFAULT NULL,\n" +
                "Dui_4 STRING DEFAULT NULL,\n" +
                "Dui_5 STRING DEFAULT NULL,\n" +
                "Time_stamp STRING DEFAULT NULL\n" +
                ");";
        database.execSQL(queryCreate);
    }
}
