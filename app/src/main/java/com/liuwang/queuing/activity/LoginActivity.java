package com.liuwang.queuing.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.liuwang.queuing.R;
import com.liuwang.queuing.util.Constants;
import com.liuwang.queuing.util.DataCache;
import com.liuwang.queuing.util.SPHelper;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    private EditText etUserName;
    private EditText etPwd;
    private TextView tvTip;

    private SPHelper spHelper;

    @Override
    protected void onFirst(Bundle saveInstanceState) {
        super.onFirst(saveInstanceState);
        spHelper=SPHelper.getInstant(getApplicationContext());
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_login;
    }

    @Override
    protected String setTitle() {
        return "登录";
    }

    @Override
    protected void instantiateView() {
        super.instantiateView();
        etUserName=findViewById(R.id.userName);
        etPwd=findViewById(R.id.pwd);
        tvTip=findViewById(R.id.tip);
    }

    @Override
    protected void initViewData() {
        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), SettingActivity.class), 1);
            }
        });
        findViewById(R.id.signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setTipInfo() {
        String baseUrl = DataCache.getBaseUrl(getApplicationContext());
        if (!TextUtils.isEmpty(baseUrl)) tvTip.setText("您的登陆请求地址为:\n" + baseUrl + "Users/Login");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 2) {
            setTipInfo();
        }
    }

    private void signIn() {
        String platformAddress = spHelper.getStringFromSP(getApplicationContext(), Constants.SETTING_PLATFORM_ADDRESS);
        String port = spHelper.getStringFromSP(getApplicationContext(), Constants.SETTING_PORT);

        final String userName = etUserName.getText().toString();
        final String pwd = etPwd.getText().toString();
        if (TextUtils.isEmpty(platformAddress) || TextUtils.isEmpty(port)) {
            Toast.makeText(getApplicationContext(), "请设置云平台信息", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(getApplicationContext(), "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        final NetWorkBusiness netWorkBusiness = new NetWorkBusiness("", DataCache.getBaseUrl(getApplicationContext()));
        netWorkBusiness.signIn(new SignIn(userName, pwd), new NCallBack<BaseResponseEntity<User>>(LoginActivity.this) {
            @Override
            protected void onResponse(BaseResponseEntity<User> response) {

            }

            @Override
            public void onResponse(Call<BaseResponseEntity<User>> call, Response<BaseResponseEntity<User>> response) {
                final Gson gson = new Gson();
                //通过返回对象-response，获取返回对象BaseResponseEntity（包含请求的返回信息内容）
                BaseResponseEntity<User> baseResponseEntity = response.body();
                Log.d("onResponse: ", "signIn, baseResponseEntity:"+ gson.toJson(baseResponseEntity));
                if (baseResponseEntity != null) {
                    if (baseResponseEntity.getStatus() == 0) {
                        DataCache.updateUserName(getApplicationContext(), userName);
                        DataCache.updatePwd(getApplicationContext(), pwd);
                        //从返回对象中获取Token值，并保存在本地
                        String accessToken = baseResponseEntity.getResultObj().getAccessToken();
                        DataCache.updateAccessToken(getApplicationContext(), accessToken);
                        Log.d("onResponse: ", "signIn, accessToken: "+accessToken);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userBaseResponseEntity", baseResponseEntity);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }else{
                        Log.d("onResponse: ", ""+baseResponseEntity.getMsg());
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "请求地址出错", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
