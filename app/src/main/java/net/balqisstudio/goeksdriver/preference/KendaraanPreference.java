package net.balqisstudio.goeksdriver.preference;

import android.content.Context;
import android.content.SharedPreferences;

import net.balqisstudio.goeksdriver.model.Kendaraan;
import net.balqisstudio.goeksdriver.service.MyConfig;

/**
 * Created by Balqis Studio on 11/5/2017.
 */
public class KendaraanPreference {

    private static String KEY_ID = "ID";
    private static String KEY_JENIS_KENDARAAN = "JENIS";
    private static String KEY_MEREK = "MEREK";
    private static String KEY_TIPE = "TIPE";
    private static String KEY_NOPOL = "NOPOL";
    private static String KEY_WARNA = "WARNA";
    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    public KendaraanPreference(Context context) {
        pref = context.getSharedPreferences(MyConfig.KENDARAAN_PREF, Context.MODE_PRIVATE);
    }

    public void insertKendaraan(Kendaraan kendaraan) {
        editor = pref.edit();
        editor.putString(KEY_ID, kendaraan.id);
        editor.putString(KEY_JENIS_KENDARAAN, kendaraan.jenisKendaraan);
        editor.putString(KEY_MEREK, kendaraan.merek);
        editor.putString(KEY_TIPE, kendaraan.tipe);
        editor.putString(KEY_NOPOL, kendaraan.nopol);
        editor.putString(KEY_WARNA, kendaraan.warna);
        editor.commit();
    }

    public void updateKendaraan(Kendaraan kendaraan) {
        editor = pref.edit();
        editor.putString(KEY_ID, kendaraan.id);
        editor.putString(KEY_MEREK, kendaraan.merek);
        editor.putString(KEY_TIPE, kendaraan.tipe);
        editor.putString(KEY_NOPOL, kendaraan.nopol);
        editor.putString(KEY_WARNA, kendaraan.warna);
        editor.commit();
    }

    public Kendaraan getKendaraan() {
        Kendaraan instance = new Kendaraan();
        instance.id = pref.getString(KEY_ID, "");
        instance.jenisKendaraan = pref.getString(KEY_JENIS_KENDARAAN, "");
        instance.merek = pref.getString(KEY_MEREK, "");
        instance.tipe = pref.getString(KEY_TIPE, "");
        instance.nopol = pref.getString(KEY_NOPOL, "");
        instance.warna = pref.getString(KEY_WARNA, "");
        return instance;
    }

    public void delete() {
        editor = pref.edit();
        editor.putString(KEY_ID, "");
        editor.putString(KEY_JENIS_KENDARAAN, "");
        editor.putString(KEY_NOPOL, "");
        editor.putString(KEY_TIPE, "");
        editor.putString(KEY_WARNA, "");
        editor.putString(KEY_MEREK, "");
        editor.commit();
    }
}