package com.cd.room.pojo;

public enum MahJongType {
    TONG {
        @Override
        public int getTotalType() {
            return 9;
        }

        @Override
        public String toString() {
            return "筒";
        }
    },
    SUO {
        @Override
        public int getTotalType() {
            return 9;
        }

        @Override
        public String toString() {
            return "索";
        }
    },
    WANG {
        @Override
        public int getTotalType() {
            return 9;
        }

        @Override
        public String toString() {
            return "万";
        }
    },
    FENG_D {
        @Override
        public int getTotalType() {
            return 1;
        }

        @Override
        public String toString() {
            return "东风";
        }
    },
    FENG_N {
        @Override
        public int getTotalType() {
            return 1;
        }

        @Override
        public String toString() {
            return "南风";
        }
    },
    FENG_X {
        @Override
        public int getTotalType() {
            return 1;
        }

        @Override
        public String toString() {
            return "西风";
        }
    },
    FENG_B {
        @Override
        public int getTotalType() {
            return 1;
        }

        @Override
        public String toString() {
            return "北风";
        }
    },
    ZHONG {
        @Override
        public int getTotalType() {
            return 1;
        }

        @Override
        public String toString() {
            return "中";
        }
    },
    FA {
        @Override
        public int getTotalType() {
            return 1;
        }

        @Override
        public String toString() {
            return "发财";
        }
    },
    BAI {
        @Override
        public int getTotalType() {
            return 1;
        }

        @Override
        public String toString() {
            return "白板";
        }
    };//筒、索、万、东、南、西、北风、中、发、白

    public abstract int getTotalType();//该种大类型的牌有几种小类型
}
