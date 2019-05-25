package com.nexless.sfbaglock.bean;

import java.util.List;

/**
 * @date: 2019/5/24
 * @author: su qinglin
 * @description:
 */
public class UploadCsvResponse {

    /**
     * totalCount : 1
     * succCount : 0
     * errCount : 1
     * errs : [{"line":2,"originalValue":"SCH2019052011030","errMsg":"该公司下无此项目编号"}]
     */

    private int totalCount;
    private int succCount;
    private int errCount;
    private List<ErrsBean> errs;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getSuccCount() {
        return succCount;
    }

    public void setSuccCount(int succCount) {
        this.succCount = succCount;
    }

    public int getErrCount() {
        return errCount;
    }

    public void setErrCount(int errCount) {
        this.errCount = errCount;
    }

    public List<ErrsBean> getErrs() {
        return errs;
    }

    public void setErrs(List<ErrsBean> errs) {
        this.errs = errs;
    }

    public static class ErrsBean {
        /**
         * line : 2
         * originalValue : SCH2019052011030
         * errMsg : 该公司下无此项目编号
         */

        private int line;
        private String originalValue;
        private String errMsg;

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public String getOriginalValue() {
            return originalValue;
        }

        public void setOriginalValue(String originalValue) {
            this.originalValue = originalValue;
        }

        public String getErrMsg() {
            return errMsg;
        }

        public void setErrMsg(String errMsg) {
            this.errMsg = errMsg;
        }
    }
}
