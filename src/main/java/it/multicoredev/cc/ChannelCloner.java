package it.multicoredev.cc;

import it.multicoredev.cc.storage.Database;
import it.multicoredev.cc.storage.Settings;
import it.multicoredev.cc.storage.Locale;
import it.multicoredev.mclib.json.GsonHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright © 2021 - 2022 by Lorenzo Magni
 * This file is part of ChannelCloner.
 * ChannelCloner is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class ChannelCloner {
    public static final Logger LOGGER = LoggerFactory.getLogger("ChannelCloner");
    private static final GsonHelper gson = new GsonHelper();
    private static final File settingsFile = new File("settings.json");
    private static final File localizationsDir = new File("localizations");
    private Settings settings;
    private final Map<DiscordLocale, Locale> localizations = new HashMap<>();
    private Database db;
    private JDA jda;

    public void main() {
        System.out.println("   ____ _                            _    ____ _                       \n" +
                "  / ___| |__   __ _ _ __  _ __   ___| |  / ___| | ___  _ __   ___ _ __ \n" +
                " | |   | '_ \\ / _` | '_ \\| '_ \\ / _ \\ | | |   | |/ _ \\| '_ \\ / _ \\ '__|\n" +
                " | |___| | | | (_| | | | | | | |  __/ | | |___| | (_) | | | |  __/ |   \n" +
                "  \\____|_| |_|\\__,_|_| |_|_| |_|\\___|_|  \\____|_|\\___/|_| |_|\\___|_|\n");
        System.out.println("by MultiCore Network");
        System.out.println("\tdeveloped by LoreSchaeffer");

        LOGGER.info("Loading settings...");
        try {
            settings = gson.autoload(settingsFile, new Settings().init(), Settings.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(-1);
        }

        LOGGER.info("Loading localizations...");
        loadLocalizations();

        LOGGER.info("Initializing database...");
        try {
            db = new Database(new File("storage.db"));
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(-2);
        }

        LOGGER.info("Starting bot...");
        try {
            if (settings.getToken() == null || settings.getToken().trim().isEmpty())
                throw new IllegalArgumentException("Bot token cannot be null or empty!");

            jda = JDABuilder.createDefault(settings.getToken())
                    .setAutoReconnect(true)
                    .setActivity(Activity.watching("https://multicore.network/discord"))
                    .addEventListeners(new EventListener(this))
                    .build()
                    .awaitReady();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(-3);
        }

        LOGGER.info("Registering bot commands...");
        CommandListUpdateAction commands = jda.updateCommands()
                .addCommands(
                        Commands.slash("cc", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("cc"))
                                .setDescriptionLocalizations(getCommandDescriptions("cc"))
                                .addSubcommands(
                                        new SubcommandData("info", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("info"))
                                                .setDescriptionLocalizations(getCommandDescriptions("info")),

                                        new SubcommandData("enable", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("enable"))
                                                .setDescriptionLocalizations(getCommandDescriptions("enable")),

                                        new SubcommandData("disable", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("disable"))
                                                .setDescriptionLocalizations(getCommandDescriptions("disable")),

                                        new SubcommandData("register", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("register"))
                                                .setDescriptionLocalizations(getCommandDescriptions("register"))
                                                .addOptions(
                                                        new OptionData(OptionType.CHANNEL, "channel", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("register:channel"), true)
                                                                .setDescriptionLocalizations(getCommandDescriptions("register:channel")),
                                                        new OptionData(OptionType.STRING, "name", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("register:name"), true)
                                                                .setDescriptionLocalizations(getCommandDescriptions("register:name")),
                                                        new OptionData(OptionType.CHANNEL, "secondary", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("register:secondary"), false)
                                                                .setDescriptionLocalizations(getCommandDescriptions("register:secondary")),
                                                        new OptionData(OptionType.STRING, "secondary_name", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("register:secondary_name"), false)
                                                                .setDescriptionLocalizations(getCommandDescriptions("register:secondary_name"))
                                                ),

                                        new SubcommandData("unregister", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("unregister"))
                                                .setDescriptionLocalizations(getCommandDescriptions("unregister"))
                                                .addOptions(
                                                        new OptionData(OptionType.CHANNEL, "channel", getLocale(DiscordLocale.ENGLISH_US).commandDescriptions.get("unregister:channel"), true)
                                                                .setDescriptionLocalizations(getCommandDescriptions("unregister:channel"))
                                                )
                                ).setGuildOnly(true));

        try {
            commands.complete();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(-4);
        }

        LOGGER.info("Bot is ready");
    }

    public Database db() {
        return db;
    }

    public JDA jda() {
        return jda;
    }

    public Locale getLocale(DiscordLocale locale) {
        if (localizations.containsKey(locale)) return localizations.get(locale);
        else return localizations.get(DiscordLocale.ENGLISH_US);
    }

    public Map<DiscordLocale, String> getCommandDescriptions(String id) {
        Map<DiscordLocale, String> descriptions = new HashMap<>();

        localizations.forEach((discordLocale, locale) -> {
            if (!locale.commandDescriptions.containsKey(id)) return;

            descriptions.put(discordLocale, locale.commandDescriptions.get(id));
        });

        return descriptions;
    }

    private void loadLocalizations() {
        if (!localizationsDir.exists() || !localizationsDir.isDirectory()) {
            if (!localizationsDir.mkdirs()) {
                LOGGER.error("Could not create localizations directory!");
                System.exit(-5);
            }
        }

        File[] files = localizationsDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (!file.isFile() || !file.getName().toLowerCase().endsWith(".json")) continue;

                try {
                    Locale locale = gson.load(file, Locale.class);
                    if (locale == null || locale.getLocale().equals(DiscordLocale.UNKNOWN)) continue;

                    locale.init();
                    gson.save(locale, file);

                    localizations.put(locale.getLocale(), locale);
                } catch (Exception e) {
                    LOGGER.warn("Cannot load localization " + file.getName() + ": " + e.getMessage());
                }
            }
        }

        if (localizations.isEmpty() || !localizations.containsKey(DiscordLocale.ENGLISH_US)) {
            Locale en = new Locale(DiscordLocale.ENGLISH_US).init();
            localizations.put(en.getLocale(), en);

            try {
                gson.save(en, new File(localizationsDir, en.getLocale().getLocale() + ".json"));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                System.exit(-6);
            }
        }

        LOGGER.info("Loaded " + localizations.size() + " localizations");
    }
}
