package com.sheet.cellflow.exception;

public enum ExceptionMapper {
    NULL_COLUMN_ID  {
        @Override
        public String getError() {
            return "columnId cannot be null";
        }
    },
    NEGATIVE_TARGET_ROW  {
        @Override
        public String getError() {
            return "Target row must be positive";
        }
    },
    UNDEFINED_SHEET_ID  {
        @Override
        public String getError() {
            return "Sheet does not exist";
        }
    },
    INVALID_COLUMN_TYPE {
        @Override
        public String getError() {
            return "Incompatible column type: ";
        }
    },
    DUPLICATE_COLUMN_NAME {
        @Override
        public String getError() {
            return "There is more then 1 column with the same name, provide column id";
        }
    },
    SAVE_COLUMN_FAILED {
        @Override
        public String getError() {
            return "Save Column failed ";
        }
    },
    INVALID_LOOKUP_TARGET_COLUMN_TYPE_NOT_MATCHED {
        @Override
        public String getError() {
            return "Invalid lookup - target column type does not match source column type";
        }
    },
    INVALID_LOOKUP_TARGET_CELL_IS_CURRENT_CELL {
        @Override
        public String getError() {
            return "Invalid lookup - target cell is current cell";
        }
    },
    CYCLE_FAILED {
        @Override
        public String getError() {
            return "Invalid lookup - There is cycle in this lookup";
        }
    },
    SHEET_CREATE_FAILED {
        @Override
        public String getError() {
            return "Set Sheet failed";
        }
    },
    COLUMN_NAME_NULL {
        @Override
        public String getError() {
            return "Column name cannot be null";
        }
    },
    COLUMN_TYPE_NULL {
        @Override
        public String getError() {
            return "Column type cannot be null";
        }
    },
    COLUMN_NAME_EMPTY {
        @Override
        public String getError() {
            return "Column name cannot be empty";
        }
    },
    MAX_LOOKUP_CHAIN_REACHED {
        @Override
        public String getError() {
            return "Max lookup chain reached";
        }
    };
    public abstract String getError();
}
