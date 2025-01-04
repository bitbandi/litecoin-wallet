/*
 * Copyright the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.schildbach.wallet.litecoin.ui.send;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import de.schildbach.wallet.litecoin.Constants;
import de.schildbach.wallet.litecoin.R;
import de.schildbach.wallet.litecoin.data.PaymentIntent;
import de.schildbach.wallet.litecoin.service.BlockchainService;
import de.schildbach.wallet.litecoin.ui.AbstractWalletActivity;
import de.schildbach.wallet.litecoin.ui.Event;
import de.schildbach.wallet.litecoin.ui.HelpDialogFragment;
import org.litecoinj.core.Coin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Schildbach
 */
public final class SendCoinsActivity extends AbstractWalletActivity {
    public static final String INTENT_EXTRA_PAYMENT_INTENT = "payment_intent";
    public static final String INTENT_EXTRA_FEE_CATEGORY = "fee_category";

    private static final Logger log = LoggerFactory.getLogger(SendCoinsActivity.class);

    public static void start(final Context context, final PaymentIntent paymentIntent,
            final @Nullable FeeCategory feeCategory, final int intentFlags) {
        final Intent intent = startIntent(context, paymentIntent, feeCategory, intentFlags);
        context.startActivity(intent);
    }

    public static Intent startIntent(final Context context, final PaymentIntent paymentIntent,
                                      final FeeCategory feeCategory, final int intentFlags) {
        final Intent intent = new Intent(context, SendCoinsActivity.class);
        intent.putExtra(INTENT_EXTRA_PAYMENT_INTENT, paymentIntent);
        if (feeCategory != null)
            intent.putExtra(INTENT_EXTRA_FEE_CATEGORY, feeCategory);
        if (intentFlags != 0)
            intent.setFlags(intentFlags);
        return intent;
    }

    public static void start(final Context context, final PaymentIntent paymentIntent) {
        start(context, paymentIntent, null, 0);
    }

    public static void startDonate(final Context context, final Coin amount, final @Nullable FeeCategory feeCategory,
            final int intentFlags) {
        start(context, PaymentIntent.from(Constants.DONATION_ADDRESS,
                context.getString(R.string.wallet_donate_address_label), amount), feeCategory, intentFlags);
    }

    private SendCoinsActivityViewModel viewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        EdgeToEdge.enable(this, SystemBarStyle.dark(getColor(R.color.bg_action_bar)),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        super.onCreate(savedInstanceState);
        log.info("Referrer: {}", getReferrer());
        setContentView(R.layout.send_coins_content);
        final Toolbar appbar = findViewById(R.id.send_coins_appbar);
        appbar.getNavigationIcon().setTint(getColor(R.color.fg_on_dark_bg_network_significant));
        setActionBar(appbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        viewModel = new ViewModelProvider(this).get(SendCoinsActivityViewModel.class);
        viewModel.showHelpDialog.observe(this, new Event.Observer<Integer>() {
            @Override
            protected void onEvent(final Integer messageResId) {
                HelpDialogFragment.page(getSupportFragmentManager(), messageResId);
            }
        });

        addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(final Menu menu, final MenuInflater inflater) {
                inflater.inflate(R.menu.send_coins_activity_options, menu);
            }

            @Override
            public void onPrepareMenu(final Menu menu) {
            }

            @Override
            public boolean onMenuItemSelected(final MenuItem item) {
                if (item.getItemId() == R.id.send_coins_options_help) {
                    viewModel.showHelpDialog.setValue(new Event<>(R.string.help_send_coins));
                    return true;
                }
                return false;
            }
        });

        BlockchainService.start(this, false);
    }
}
