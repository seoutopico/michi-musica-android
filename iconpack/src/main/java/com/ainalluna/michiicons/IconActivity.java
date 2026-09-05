package com.ainalluna.michiicons;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/** Three bundled images only. No filesystem, network or account access. */
public class IconActivity extends Activity {
    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(24), dp(40), dp(24), dp(24));
        column.setBackgroundColor(Color.rgb(9, 10, 27));
        column.setOnApplyWindowInsetsListener((view,insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(android.view.WindowInsets.Type.systemBars());
                view.setPadding(dp(24)+bars.left, dp(24)+bars.top, dp(24)+bars.right, dp(24)+bars.bottom);
            } else {
                view.setPadding(dp(24)+insets.getSystemWindowInsetLeft(), dp(24)+insets.getSystemWindowInsetTop(),
                    dp(24)+insets.getSystemWindowInsetRight(), dp(24)+insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        TextView title = text("Michi Iconos", 30); column.addView(title);
        column.addView(text("En Niagara, mantén pulsada Michi Música, toca su icono y elige Michi Iconos. Puedes escoger Medianoche, Rosa o el gato transparente.", 16));
        String[] labels = {"Medianoche", "Rosa", "Transparente"};
        int[] icons = {R.drawable.michi_midnight, R.drawable.michi_rose, R.drawable.michi_outline};
        for (int i=0;i<icons.length;i++) {
            final int icon = icons[i];
            LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0,dp(12),0,dp(12));
            ImageView image = new ImageView(this); image.setImageResource(icon);
            row.addView(image,new LinearLayout.LayoutParams(dp(80),dp(80)));
            TextView label=text(labels[i],18); row.addView(label);
            row.setContentDescription(labels[i]); row.setFocusable(true);
            row.setOnClickListener(view -> select(icon)); column.addView(row);
        }
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(column); setContentView(scroll);
    }
    private TextView text(String value,int size) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size);
        view.setTextColor(Color.rgb(248,245,250)); view.setPadding(0,dp(8),0,dp(12));return view;
    }
    private int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
    @SuppressWarnings("deprecation") private void select(int resource) {
        String action=getIntent().getAction();
        if (Intent.ACTION_GET_CONTENT.equals(action) || Intent.ACTION_PICK.equals(action) || "org.adw.launcher.icons.ACTION_PICK_ICON".equals(action)) {
            Bitmap original=BitmapFactory.decodeResource(getResources(),resource);
            Bitmap icon=Bitmap.createScaledBitmap(original,192,192,true);
            Intent result=new Intent().setData(Uri.parse("android.resource://"+getPackageName()+"/"+resource));
            result.putExtra(Intent.EXTRA_SHORTCUT_ICON,icon);
            result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,Intent.ShortcutIconResource.fromContext(this,resource));
            setResult(RESULT_OK,result);finish();
        } else Toast.makeText(this,"Elige este icono desde el menú de Michi Música en Niagara.",Toast.LENGTH_LONG).show();
    }
}
