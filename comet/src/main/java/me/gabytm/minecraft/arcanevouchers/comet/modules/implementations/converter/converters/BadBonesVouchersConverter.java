package me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter.converters;

import me.gabytm.minecraft.arcanevouchers.comet.modules.implementations.converter.Converter;
import me.gabytm.minecraft.arcanevouchers.comet.utils.Numbers;
import me.gabytm.minecraft.arcanevouchers.comet.utils.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BadBonesVouchersConverter extends Converter {

    private final Map<String, Pattern> placeholders = new HashMap<>();
    private final Pattern randomPattern = Pattern.compile("%random%:(\\d+)-(\\d+)", Pattern.CASE_INSENSITIVE);

    public BadBonesVouchersConverter() {
        super("Config.yml");
        placeholders.put("%player_name%", Pattern.compile("%player%", Pattern.CASE_INSENSITIVE));
        placeholders.put("%player_world%", Pattern.compile("%world%", Pattern.CASE_INSENSITIVE));
        placeholders.put("%player_x%", Pattern.compile("%[xX]%"));
        placeholders.put("%player_y%", Pattern.compile("%[yY]%"));
        placeholders.put("%player_z%", Pattern.compile("%[zZ]%"));
        placeholders.put("%args[1]%", Pattern.compile("%arg%", Pattern.CASE_INSENSITIVE));
    }

    @Contract("_ -> !null")
    @NotNull
    private List<String> updateCommands(@NotNull final List<String> commands) {
        return commands.stream()
                .map(this::update)
                .map(it -> "[command] " + it)
                .collect(Collectors.toList());
    }

    @Contract("_, _ -> !null")
    @NotNull
    private String update(@NotNull String string, final boolean updateColors) {
        for (Map.Entry<String, Pattern> entry : placeholders.entrySet()) {
            final Matcher matcher = entry.getValue().matcher(string);

            if (matcher.find()) {
                string = matcher.replaceAll(entry.getKey());
            }
        }

        final Matcher randomMatcher = randomPattern.matcher(string);

        if (randomMatcher.find()) {
            string = randomMatcher.replaceAll("randomL:{$1,$2}");
        }

        return updateColors ? Strings.upgradeColorsFormat(string) : string;
    }

    @Contract("_ -> !null")
    @NotNull
    private String update(@NotNull final String string) {
        return update(string, true);
    }

    @Contract("_, _ -> !null")
    @NotNull
    private List<String> update(@NotNull final List<String> list, final boolean updateColors) {
        return list.stream().map(it -> update(it, updateColors)).collect(Collectors.toList());
    }

    @Contract("_ -> !null")
    @NotNull
    private List<String> update(@NotNull final List<String> list) {
        return update(list, true);
    }

    private void convertItem(@NotNull final CommentedConfigurationNode source, @NotNull final CommentedConfigurationNode target) throws SerializationException {
        final String material = source.node("Item").getString();

        if (material == null) {
            return;
        }

        final String[] materialParts = material.split(":", 2);

        if (materialParts.length == 1) {
            target.node("material").set(material);
        } else {
            target.node("material").set(materialParts[0]);
            target.node("damage").set(Numbers.tryParseShort(materialParts[1], (short) 0));
        }

        moveString(source, "Name", target, "name", this::update);
        moveList(source, "Lore", target, "lore", this::update);
        moveString(source, "Player", target, "texture", (texture) -> {
            if (texture.length() >= 3 && texture.length() <= 16) {
                return "PLAYER;" + texture;
            }

            return texture;
        });
        moveBoolean(source, "Glowing", target, "glow", true);
    }

    private void convertSettings(@NotNull final CommentedConfigurationNode source, @NotNull final CommentedConfigurationNode target) throws SerializationException {
        moveString(source, "Message", target, "messages.receive", this::update);

        /*final CommentedConfigurationNode whitelistWorlds = source.node("Whitelist-Worlds");

        if (!whitelistWorlds.virtual() && !whitelistWorlds.empty()) {
            target.node("worlds", "whitelist").act(node -> {
               moveString(whitelistWorlds, "Message", node, "message", this::update);
               moveList(whitelistWorlds, "Worlds", node, "list");
            });
        }*/

        source.node("Whitelist-Worlds").act(whitelistWorlds -> {
            if (whitelistWorlds.virtual() || whitelistWorlds.empty()) {
                return;
            }

            target.node("worlds", "whitelist").act(node -> {
                moveString(whitelistWorlds, "Message", node, "message", this::update);
                moveList(whitelistWorlds, "Worlds", node, "list");
            });
        });

        source.node("Permission").act(node -> {
            if (node.virtual() || node.empty()) {
                return;
            }

            final CommentedConfigurationNode permissions = target.node("permissions");

            node.node("Whitelist-Permission").act(whitelist -> {
                moveString(whitelist, "Message", permissions, "whitelist.message", this::update);
                moveList(whitelist, "Permissions", permissions, "whitelist.list", this::update);
            });

            node.node("Blacklist-Permissions").act(blacklist -> {
                moveString(blacklist, "Message", permissions, "blacklist.message", this::update);
                moveList(blacklist, "Permissions", permissions, "blacklist.list", this::update);
            });
        });

        source.node("Limiter").act(limiter -> {
            if (limiter.virtual() || limiter.empty()) {
                return;
            }

            moveBoolean(limiter, "Toggle", target, "limit.enabled", false);
            moveLong(limiter, "Limit", target, "limit.limit");
            target.node("limit", "type").set("GLOBAL");
        });
        
        moveBoolean(source, "Two-Step-Verification.Toggle", target, "confirmation.enabled", true);
    }

    // TODO: 1/18/2022 convert Items
    @Override
    public boolean convert(final CommentedConfigurationNode source, final CommentedConfigurationNode target) throws SerializationException {
        final CommentedConfigurationNode vouchersNode = source.node("Vouchers");

        if (vouchersNode.virtual() || vouchersNode.empty()) {
            return false;
        }

        for (final CommentedConfigurationNode voucherSection : vouchersNode.childrenMap().values()) {
            final CommentedConfigurationNode targetVoucherSection = target.node("vouchers", voucherSection.key());

            convertItem(voucherSection, targetVoucherSection.node("item"));
            moveList(voucherSection, "Commands", targetVoucherSection, "actions", this::updateCommands);

            final CommentedConfigurationNode options = voucherSection.node("Options");

            if (!options.virtual() && !options.empty()) {
                convertSettings(options, targetVoucherSection.node("settings"));
            }
        }
        return true;
    }

}
