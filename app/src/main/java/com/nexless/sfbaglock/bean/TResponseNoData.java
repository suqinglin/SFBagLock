package com.nexless.sfbaglock.bean;

import com.nexless.sfbaglock.AppConstant;

/**
 * Created by Calm on 2017/12/5.
 * TResponseNoData
 */

public class TResponseNoData
{
    public int code;
    public String message;
    public boolean isSuccess()
    {
        return AppConstant.RESPONSE_CODE_SUCCESS == code;
    }
}
