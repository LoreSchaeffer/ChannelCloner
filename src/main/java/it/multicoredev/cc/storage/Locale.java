package it.multicoredev.cc.storage;

import com.google.gson.annotations.SerializedName;
import it.multicoredev.mclib.json.JsonConfig;
import net.dv8tion.jda.api.interactions.DiscordLocale;

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
public class Locale extends JsonConfig {
    protected String locale;
    @SerializedName("invalid_command")
    public String invalidCommand;
    @SerializedName("insufficient_permissions")
    public String insufficientPerms;
    @SerializedName("insufficient_bot_permissions")
    public String insufficientBotPerms;
    @SerializedName("internal_error")
    public String internalError;
    @SerializedName("not_audio_channel")
    public String notAudioChannel;
    @SerializedName("invalid_name")
    public String invalidName;
    @SerializedName("template_not_registered")
    public String templateNotRegistered;
    @SerializedName("already_registered")
    public String alreadyRegistered;


    public String enabled;
    public String disabled;
    @SerializedName("template_registered")
    public String templateRegistered;
    @SerializedName("template_registered_secondary")
    public String templateRegisteredSecondary;
    @SerializedName("template_unregistered")
    public String templateUnregistered;
    @SerializedName("template_unregistered_secondary")
    public String templateUnregisteredSecondary;
    public String info;

    @SerializedName("command_descriptions")
    public Map<String, String> commandDescriptions;

    public Locale(DiscordLocale locale) {
        this.locale = locale.getLocale();
    }

    @Override
    public Locale init() {
        if (invalidCommand == null) invalidCommand = "Invalid command syntax!";
        if (insufficientPerms == null) insufficientPerms = "Insufficient permissions!";
        if (insufficientBotPerms == null) insufficientBotPerms = "The bot must have Permission.MANAGE_PERMISSIONS on the channel explicitly in order to set permissions it doesn't already have!";
        if (internalError == null) internalError = "Internal error: {error}.";
        if (notAudioChannel == null) notAudioChannel = "You must provide an Audio Channel, not a Text Channel.";
        if (invalidName == null) invalidName = "Invalid name! Name must be between 1 and 100 characters long.";
        if (templateNotRegistered == null) templateNotRegistered = "This template is not registered.";
        if (alreadyRegistered == null) alreadyRegistered = "This template is already registered.";

        if (enabled == null) enabled = ":white_check_mark: Channel Cloner enabled in this server.";
        if (disabled == null) disabled = ":x: Channel Cloner disabled in this server.";
        if (templateRegistered == null) templateRegistered = ":dna: Template registered. Now when the channel will be occupied, a new channel will be generated with the name {name}.";
        if (templateRegisteredSecondary == null) templateRegisteredSecondary = ":dna: Template registered. Now when the channel will be occupied, a new channel will be generated with the name {name} with a secondary channel with the name {secondary}.";
        if (templateUnregistered == null) templateUnregistered = ":recycle: Template unregistered. Now the channel won't be cloned anymore.";
        if (templateUnregisteredSecondary == null) templateUnregisteredSecondary = ":recycle: Template unregistered. Now the secondary channel won't be cloned anymore.";
        if (info == null) info = ":globe_with_meridians: Welcome to Channel Cloner by LoreSchaeffer.\nDo you need help?\n:one: Check the documentation here: https://multicore.network\n:two: Ask us on Discord: https://multicore.network/discord";

        if (commandDescriptions == null) commandDescriptions = new HashMap<>();

        if (!commandDescriptions.containsKey("cc")) commandDescriptions.put("cc", "Allows you to manage the Channel Cloner bot");
        if (!commandDescriptions.containsKey("info")) commandDescriptions.put("info", "Shows information about the bot");
        if (!commandDescriptions.containsKey("enable")) commandDescriptions.put("enable", "Enables the Channel Cloner bot in this server");
        if (!commandDescriptions.containsKey("disable")) commandDescriptions.put("disable", "Disables the Channel Cloner bot in this server");
        if (!commandDescriptions.containsKey("register")) commandDescriptions.put("register", "Add a voice channel as a model to be cloned");
        if (!commandDescriptions.containsKey("register:channel")) commandDescriptions.put("register:channel", "The channel to register as a model");
        if (!commandDescriptions.containsKey("register:name")) commandDescriptions.put("register:name", "The format of the name of the cloned channels (Use %d for the number of the channel)");
        if (!commandDescriptions.containsKey("register:secondary")) commandDescriptions.put("register:secondary", "If the channel has a secondary channel, set it here");
        if (!commandDescriptions.containsKey("register:secondary_name")) commandDescriptions.put("register:secondary_name", "The format of the name of the cloned secondary channels (Use %d for the number of the channel)");
        if (!commandDescriptions.containsKey("unregister")) commandDescriptions.put("unregister", "Remove a voice channel from the models");
        if (!commandDescriptions.containsKey("unregister:channel")) commandDescriptions.put("unregister:channel", "The channel to unregister");

        return this;
    }

    public DiscordLocale getLocale() {
        if (locale == null) return DiscordLocale.UNKNOWN;
        return DiscordLocale.from(locale);
    }
}
