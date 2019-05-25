package com.nexless.sfbaglock.bean;

import java.util.List;

/**
 * @date: 2019/5/23
 * @author: su qinglin
 * @description:
 */
public class ProjectsResponse {

    /**
     * companyCode : SQH
     *  projects  : []
     * userId : 4
     * userKey : 1234567890adcd
     */

    private String companyCode;
    private String userId;
    private String userKey;
    private List<ProjectBean> projects;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public List<ProjectBean> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectBean> projects) {
        this.projects = projects;
    }

    public class ProjectBean {
        /**
         * companyCode : SQH
         * projectNo : SCH20190520110305649
         * skuName : 1号箱
         * start : 0119100000
         * end : 0119100002
         */

        private String companyCode;
        private String projectNo;
        private String skuName;
        private String start;
        private String end;

        public String getCompanyCode() {
            return companyCode;
        }

        public void setCompanyCode(String companyCode) {
            this.companyCode = companyCode;
        }

        public String getProjectNo() {
            return projectNo;
        }

        public void setProjectNo(String projectNo) {
            this.projectNo = projectNo;
        }

        public String getSkuName() {
            return skuName;
        }

        public void setSkuName(String skuName) {
            this.skuName = skuName;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }
    }

}
