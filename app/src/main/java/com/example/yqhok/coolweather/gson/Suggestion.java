package com.example.yqhok.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by yqhok on 2017/5/24.
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public Carwash carwash;

    @SerializedName("drsg")
    public Dress dress;

    @SerializedName("flu")
    public Flu flu;

    @SerializedName("sport")
    public Sport sport;

    @SerializedName("trav")
    public Travel travel;

    @SerializedName("uv")
    public Uv uv;

    public class Comfort {

        @SerializedName("txt")
        public String info;

        @SerializedName("brf")
        public String brf;

    }

    public class Carwash {

        @SerializedName("txt")
        public String info;

        @SerializedName("brf")
        public String brf;

    }

    public class Dress {

        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }

    public class Flu {

        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }

    public class Sport {

        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;

    }

    public class Travel {

        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }

    public class Uv {

        @SerializedName("brf")
        public String brf;

        @SerializedName("txt")
        public String info;
    }

}
