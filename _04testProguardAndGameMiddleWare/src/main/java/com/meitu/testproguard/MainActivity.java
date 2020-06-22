package com.meitu.testproguard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.meitu.mtgamemiddlewaresdk.MtgameMiddlewareSdk;
import com.meitu.mtgamemiddlewaresdk.model.GameModel;

public class MainActivity extends AppCompatActivity {

    private EditText editGameUrl;
    private Button buttonOpenEgret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        MtgameMiddlewareSdk.init(getApplication(), true);
    }

    private void initView() {
        editGameUrl = findViewById(R.id.edit_game_url);
        editGameUrl.setText("http://biz-site.zone1.meitudata.com/minigame_entry_update.rpk");
        buttonOpenEgret = findViewById(R.id.button_open_egret);
        buttonOpenEgret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameModel gameModel = new GameModel(false, "111", "http://biz-site.zone1.meitudata.com/minigame_entry_update.rpk",
                        null, null, null, null);
                MtgameMiddlewareSdk.handleGame(MainActivity.this, gameModel);
                /*String gameUrl = editGameUrl.getText().toString();
                if (!TextUtils.isEmpty(gameUrl) && (gameUrl.startsWith("https") || gameUrl.startsWith("http"))) {
                    String meituUrl = "http://www.meitu.com/";
                    String gameExtendParams = "{\"iosBackUrl\":\"" + meituUrl + "\",\"androidBackUrl\":\"" + meituUrl + "\",\"backUrlType\":0}";
                    gameUrl = editGameUrl.getText().toString();
                    Uri uri0 = Uri.parse("mtec://mtgame/wechatgame?gamePlatform=egret&gameId=1&gameUrl=" + gameUrl +
                            "&adConfigId=hahapage&gameSubpackUrl=xxx&gameExtendParams=" + gameExtendParams);
                    MTSchemeTransfer.getInstance().processUri(MainActivity.this, uri0);
                } else {
                    Toast.makeText(MainActivity.this, "请输入正确的游戏rpk下载地址", Toast.LENGTH_SHORT).show();
                }*/
            }
        });
    }

}
