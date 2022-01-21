package me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter.converters;

import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter.Converter;
import me.gabytm.minecraft.arcanevouchers.comet.utils.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SongodaVouchersConverter extends Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SongodaVouchersConverter.class);

    private final Map<String, Pattern> placeholders = new HashMap<>();

    public SongodaVouchersConverter() {
        super("EpicVouchers.yml");
        placeholders.put("%player_name%", Pattern.compile("%player%", Pattern.CASE_INSENSITIVE));
    }

    @NotNull
    private String updateString(@NotNull String string, boolean updateFormatToMini) {
        for (Map.Entry<String, Pattern> entry : placeholders.entrySet()) {
            string = entry.getValue().matcher(string).replaceAll(entry.getKey());
        }

        if (updateFormatToMini) {
            string = Strings.upgradeColorsFormat(string);
        }

        return string;
    }

    private void convertItem(CommentedConfigurationNode source, CommentedConfigurationNode target) throws SerializationException {
        moveString(source, "material", target, "material");
        moveString(source, "name", target, "name", (name) -> updateString(name, true));
        moveList(source, "lore", target, "lore", (lore) ->
                lore.stream()
                        .map(line -> updateString(line, true))
                        .collect(Collectors.toList())
        );
        moveBoolean(source, "glow", target, "glow", true);
        moveBoolean(source, "unbreakable", target, "unbreakable", true);

        if (source.node("hide-attributes").getBoolean()) {
            target.node("flags").setList(String.class, Collections.singletonList("HIDE_ATTRIBUTES"));
        }
    }

    private void convertActions(CommentedConfigurationNode source, CommentedConfigurationNode target) throws SerializationException {
        final List<String> actions = new ArrayList<>();

        if (source.node("feed-player").getBoolean()) {
            actions.add("[console] feed %player_name%");
        }

        if (source.node("heal-player").getBoolean()) {
            actions.add("[console] heal %player_name%");
        }

        if (source.node("smite-effect").getBoolean()) {
            actions.add("[console] smite %player_name%");
        }

        source.node("broadcasts").act(broadcasts -> {
           if (broadcasts.empty() || !broadcasts.isList()) {
               return;
           }

           final List<String> broadcast = broadcasts.getList(String.class);

           if (broadcast == null) {
               return;
           }

           actions.add("{broadcast=*} [message] " + updateString(String.join("\\n", broadcast), true));
        });

        source.node("commands").act(commands -> {
            if (commands.empty() || !commands.isList()) {
                return;
            }

            final List<String> list = commands.getList(String.class);

            if (list == null) {
                return;
            }

            for (final String command : list) {
                if (!command.startsWith("[")) {
                    actions.add("[console] " + updateString(command, true));
                    continue;
                }

                final String[] parts = command.split("]", 2);

                if (parts.length == 1) {
                    actions.add("[console] " + updateString(command, true));
                    continue;
                }

                switch (parts[0].toLowerCase() + ']') {
                    case "[chat]": {
                        actions.add("[chat] " + updateString(parts[1], true));
                        break;
                    }

                    case "[player]":
                    case "[op]": {
                        actions.add("[player] " + updateString(parts[1], true));
                        break;
                    }

                    default: {
                        LOGGER.warn("Unknown action {}]", parts[0]);
                        actions.add(updateString(command, true));
                        break;
                    }
                }
            }
        });

        source.node("actionbar").act(actionbar -> {
            if (actionbar.virtual() || actionbar.empty()) {
                return;
            }

            final String message = actionbar.getString();

            if (message != null) {
                actions.add("{type=ACTION} [message] " + updateString(message, true));
            }
        });

        source.node("titles").act(titles -> {
            if (titles.virtual() || titles.empty()) {
                return;
            }

            actions.add(String.format(
                    "{type=TITLE fadeIn=%d stay=%d fadeOut=%d} [message] %s[n]%s",
                    titles.node("fade-in").getInt(10),
                    titles.node("stay").getInt(70),
                    titles.node("fade-out").getInt(20),
                    updateString(titles.node("title").getString(""), true),
                    updateString(titles.node("subtitle").getString(""), true)
            ));
        });

        source.node("sounds").act(sounds -> {
            if (sounds.virtual() || sounds.empty()) {
                return;
            }

            actions.add("[sound] " + sounds.node("sound").getString(""));
        });

        if (!actions.isEmpty()) {
            target.node("actions").setList(String.class, actions);
            target.node("bulkActions").setList(String.class, actions);
        }
    }

    private void convertSettings(CommentedConfigurationNode source, CommentedConfigurationNode targe) throws SerializationException {
        source.node("confirm").act(confirm -> {
            if (confirm.getBoolean()) {
                targe.node("confirmation", "enabled").set(true);
            }
        });

        source.node("coolDown").act(cooldown -> {
            if (cooldown.virtual() || cooldown.empty()) {
                return;
            }

            final int cooldownValue = cooldown.getInt();

            if (cooldownValue > 0) {
                targe.node("cooldown", "enabled").set(true);
                targe.node("cooldown", "cooldown").set(cooldownValue);
            }
        });
    }

    @Override
    public boolean convert(CommentedConfigurationNode source, CommentedConfigurationNode target) throws SerializationException {
        final CommentedConfigurationNode vouchersNode = source.node("vouchers");

        if (vouchersNode.virtual() || vouchersNode.empty()) {
            return false;
        }

        for (final CommentedConfigurationNode voucherSection : vouchersNode.childrenMap().values()) {
            final CommentedConfigurationNode targetVoucherSection = target.node("vouchers", voucherSection.key());

            convertItem(voucherSection, targetVoucherSection.node("item"));
            convertActions(voucherSection, targetVoucherSection);
            convertSettings(voucherSection, targetVoucherSection.node("settings"));
        }

        return true;
    }

}
