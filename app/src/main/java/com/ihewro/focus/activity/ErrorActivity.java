package com.ihewro.focus.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.ihewro.focus.databinding.ActivityErrorBinding;

public class ErrorActivity extends AppCompatActivity {

    private ActivityErrorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityErrorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*binding.restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //重新启动
                final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());
                assert config != null;
                CustomActivityOnCrash.restartApplication(ErrorActivity.this, config);
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
