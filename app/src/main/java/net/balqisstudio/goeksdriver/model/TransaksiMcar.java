package net.balqisstudio.goeksdriver.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Balqis Studio on 19/10/2017.
 */
public class TransaksiMcar implements Serializable{

    public String id_transaksi;
    public String id_pelanggan;
    public String reg_id_pelanggan;
    public String order_fitur;
    public String start_latitude;
    public String start_longitude;
    public String end_latitude;
    public String end_longitude;
    public String jarak;
    public String harga;
    public String waktu_order;
    public String waktu_selesai;
    public String alamat_asal;
    public String alamat_tujuan;
    public String rate;
    public String kode_promo;
    public String kredit_promo;
    public String pakai_mpay;
    public String nama_pelanggan;
    public String telepon_pelanggan;

//
    public Map<String, String> dataDummy(){

//        String reg_id = new U;
        Map<String, String> dummy = new HashMap<>();
        dummy.put("id_transaksi","107");
        dummy.put("id_pelanggan","P1");
//        dummy.put("reg_id_pelanggan", reg_id);
        dummy.put("order_fitur","1");
        dummy.put("start_latitude","-6.258073");
        dummy.put("start_longitude","106.850105");
        dummy.put("end_latitude","-6.258159");
        dummy.put("end_longitude","106.835288");
        dummy.put("jarak","2.11");
        dummy.put("harga","8000");
        dummy.put("waktu_order","2016-11-06 14:08:46");
        dummy.put("waktu_selesai",null);
        dummy.put("alamat_asal","Jalan kalibata");
        dummy.put("alamat_tujuan","Jalan duren tiga");
        dummy.put("rate",null);
        dummy.put("kode_promo","2");
        dummy.put("kredit_promo","10000");
        dummy.put("pakai_mpay","0");

        return dummy;
    }
}
