package com.coolweather.android.db;


import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by 令子 on 2017/1/21.
 */

@Table(database = AppDatabase.class)
public class Province extends BaseModel{

    @PrimaryKey(autoincrement = true)
    private int id;

    @Column
    private String provinceName;

    @Column
    private int provinceCode;

    public int getId(){
        return id;
    }

    public void setId(int id){
            this.id = id;
        }

    public String getProvinceName(){
            return provinceName;
        }

    public void setProvinceName(String provinceName){
            this.provinceName = provinceName;
        }

    public int getProvinceCode(){
            return provinceCode;
        }

    public void setProvinceCode(int provinceCode){
            this.provinceCode = provinceCode;
        }
}
