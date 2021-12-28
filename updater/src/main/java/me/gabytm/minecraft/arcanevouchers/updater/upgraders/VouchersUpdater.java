package me.gabytm.minecraft.arcanevouchers.updater.upgraders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class VouchersUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(VouchersUpdater.class);

    private final Pattern actionPattern = Pattern.compile("\\[(?<id>\\w+)] (?<data>.*)");
    private final Map<String, String> vouchersMappings = new HashMap<>();

    public VouchersUpdater() {
        // Item settings
        vouchersMappings.put("vouchers.%s.item.display_name", "vouchers.%s.item.name");
        // Voucher settings
        vouchersMappings.put("vouchers.%s.settings.bulkOpen", "vouchers.%s.settings.bulkOpen.enabled");
        vouchersMappings.put("vouchers.%s.settings.worldWhitelist", "vouchers.%s.settings.worlds.whitelist.list");
    }

    private List<String> updateActions(final List<String> actions) {
        final List<String> updatedActions = new ArrayList<>(actions.size());

        for (final String it : actions) {
            final Matcher matcher = this.actionPattern.matcher(it);

            if (!matcher.matches()) {
                LOGGER.warn("Action '{}' doesn't match the format", it);
                continue;
            }

            final String id = matcher.group("id");
            final String data = matcher.group("data");

            switch (id.toLowerCase()) {
                case "actionbar": {
                    updatedActions.add("{type=ACTION} [message] " + data);
                    break;
                }

                case "broadcast": {
                    updatedActions.add("{broadcast=*} [message] " + data);
                    break;
                }

                case "chat": {
                    updatedActions.add("{type=PLAYER} [message] " + data);
                    break;
                }

                case "permission": {
                    final String[] parts = data.split(" ", 2);

                    if (parts.length != 2) {
                        continue;
                    }

                    updatedActions.add(format("{permission=~%s} [player] %s", parts[0], parts[1]));
                    break;
                }

                case "permissionbroadcast": {
                    final String[] parts = data.split(" ", 2);

                    if (parts.length != 2) {
                        continue;
                    }

                    updatedActions.add(format("{broadcast=permission:%s} [message] %s", parts[0], parts[1]));
                    break;
                }

                // addmoney, console, message, player
                default: {
                    updatedActions.add(id);
                    break;
                }
            }
        }

        return updatedActions;
    }

}
