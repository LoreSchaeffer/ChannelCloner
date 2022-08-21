package it.multicoredev.cc;

import it.multicoredev.cc.storage.Database;
import it.multicoredev.cc.storage.Locale;
import it.multicoredev.cc.storage.models.Clone;
import it.multicoredev.cc.storage.models.SecondaryTemplate;
import it.multicoredev.cc.storage.models.Template;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class EventListener extends ListenerAdapter {
    private final ChannelCloner cc;

    public EventListener(ChannelCloner cc) {
        this.cc = cc;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (event.getGuild() == null || event.getMember() == null) return;
        Guild guild = event.getGuild();
        Member member = event.getMember();
        Locale locale = cc.getLocale(event.getUserLocale());

        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            reply(event, locale.insufficientPerms);
            return;
        }

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            reply(event, locale.invalidCommand);
            return;
        }

        if (subcommand.equals("enable")) {
            enable(event, guild, locale);
        } else if (subcommand.equals("disable")) {
            disable(event, guild, locale);
        } else if (subcommand.equals("register")) {
            register(event, guild, locale);
        } else if (subcommand.equals("unregister")) {
            unregister(event, guild, locale);
        } else if (subcommand.equals("info")) {
            info(event, guild, locale);
        } else {
            reply(event, locale.invalidCommand);
        }
    }

    private void enable(SlashCommandInteractionEvent event, Guild guild, Locale locale) {
        Database.Result result = cc.db().enableBot(guild, event.getChannel().asTextChannel());

        if (result.isSuccess()) reply(event, locale.enabled);
        else reply(event, locale.internalError.replace("{error}", result.getError()));
    }

    private void disable(SlashCommandInteractionEvent event, Guild guild, Locale locale) {
        Database.Result result = cc.db().disableBot(guild);

        if (result.isSuccess()) reply(event, locale.disabled);
        else reply(event, locale.internalError.replace("{error}", result.getError()));
    }

    private void info(SlashCommandInteractionEvent event, Guild guild, Locale locale) {
        reply(event, locale.info, 60);
    }

    private void register(SlashCommandInteractionEvent event, Guild guild, Locale locale) {
        AudioChannel channel = parseVoiceChannel(event.getOption("channel"));
        String name = parseString(event.getOption("name"));
        AudioChannel secondary = parseVoiceChannel(event.getOption("secondary"));
        String secondaryName = parseString(event.getOption("secondary_name"));

        if (channel == null || name == null || name.trim().isEmpty()) {
            reply(event, locale.invalidCommand);
            return;
        }

        if (!isVoiceChannel(event.getOption("channel"))) {
            reply(event, locale.notAudioChannel);
            return;
        }

        if (secondary != null && (secondaryName == null || secondaryName.trim().isEmpty())) {
            reply(event, locale.invalidCommand);
            return;
        }

        if (secondaryName != null && secondary == null) {
            reply(event, locale.invalidCommand);
            return;
        }

        if (secondary != null && !isVoiceChannel(event.getOption("secondary"))) {
            reply(event, locale.notAudioChannel);
            return;
        }

        if (cc.db().isRegistered(channel) || (secondary != null && cc.db().isRegistered(secondary))) {
            reply(event, locale.alreadyRegistered);
            return;
        }

        Database.Result result = cc.db().registerTemplate(channel, name, secondary);

        if (!result.isSuccess()) {
            reply(event, locale.internalError.replace("{error}", result.getError()));
            return;
        }

        if (secondary != null) {
            result = cc.db().registerSecondaryTemplate(channel, secondary, secondaryName);

            if (!result.isSuccess()) {
                reply(event, locale.internalError.replace("{error}", result.getError()));
                return;
            }

            reply(event, locale.templateRegisteredSecondary.replace("{name}", name).replace("{secondary}", secondaryName));
            return;
        }

        reply(event, locale.templateRegistered.replace("{name}", name));
    }

    private void unregister(SlashCommandInteractionEvent event, Guild guild, Locale locale) {
        AudioChannel channel = parseVoiceChannel(event.getOption("channel"));

        if (channel == null) {
            reply(event, locale.invalidCommand);
            return;
        }

        Database.Result result = cc.db().unregisterTemplate(channel);

        if (!result.isSuccess()) {
            reply(event, locale.internalError.replace("{error}", result.getError()));
            return;
        }

        if (result.getError() != null && result.getError().equals("primary")) {
            deleteAllClones(channel);
            reply(event, locale.templateUnregistered);
        } else if (result.getError() != null && result.getError().equals("secondary")) {
            reply(event, locale.templateUnregisteredSecondary);
        } else {
            reply(event, locale.templateNotRegistered);
        }
    }

    private String parseString(OptionMapping mapping) {
        if (mapping == null) return null;

        try {
            return mapping.getAsString();
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    private boolean isVoiceChannel(OptionMapping mapping) {
        if (mapping == null) return false;

        try {
            GuildChannelUnion gcu = mapping.getAsChannel();
            return gcu instanceof AudioChannel;
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    private AudioChannel parseVoiceChannel(OptionMapping mapping) {
        if (mapping == null) return null;

        try {
            GuildChannelUnion gcu = mapping.getAsChannel();
            if (gcu instanceof AudioChannel) return (AudioChannel) gcu;
        } catch (IllegalStateException ignored) {
        }

        return null;
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        joinChannel(event.getChannelJoined());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        leaveChannel(event.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        joinChannel(event.getChannelJoined());
        leaveChannel(event.getChannelLeft());
    }

    private void reply(SlashCommandInteractionEvent event, String message, int delay) {
        event.reply(message).complete().deleteOriginal().queueAfter(delay, TimeUnit.SECONDS);
    }

    private void reply(SlashCommandInteractionEvent event, String message) {
        reply(event, message, 8);
    }

    private void deleteAllClones(AudioChannel channel) {
        cc.db().getClones(channel).forEach(clone -> {
            VoiceChannel vc = channel.getGuild().getVoiceChannelById(clone.getId());
            if (vc != null) vc.delete().queue();
        });
    }

    private void joinChannel(AudioChannel channel) {
        if (!cc.db().isEnabled(channel.getGuild())) return;
        if (!cc.db().isRegistered(channel)) return;

        Template template = cc.db().getTemplate(channel);
        if (template == null) return;

        VoiceChannel templateChannel = getChannel(template.getGuild(), template.getId());
        if (templateChannel == null) return;

        List<Clone> clones = cc.db().getClones(templateChannel);

        long occupiedChannels = clones.stream().filter(this::hasMembers).count() + (templateChannel.getMembers().isEmpty() ? 0 : 1);
        if (occupiedChannels < clones.size() + 1) return;

        int number = getCloneNumber(clones);

        try {
            VoiceChannel clone = templateChannel
                    .createCopy()
                    .setPosition(templateChannel.getPosition())
                    .setName(template.getName().replace("%d", String.valueOf(number)))
                    .complete();

            VoiceChannel secondaryClone = null;
            if (template.getSecondary() != null) {
                VoiceChannel secondaryChannel = getChannel(channel.getGuild().getId(), template.getSecondary());

                SecondaryTemplate secondaryTemplate = cc.db().getSecondaryTemplate(template.getGuild(), template.getSecondary());
                if (secondaryTemplate != null) {
                    if (secondaryChannel != null) {
                        secondaryClone = secondaryChannel
                                .createCopy()
                                .setPosition(secondaryChannel.getPosition())
                                .setName(secondaryTemplate.getName().replace("%d", String.valueOf(number)))
                                .complete();
                    }
                }
            }

            cc.db().addClone(clone, templateChannel, secondaryClone, number);
        } catch (InsufficientPermissionException e) {
            try {
                TextChannel errorChannel = channel.getGuild().getTextChannelById(cc.db().getChannel(channel.getGuild()));
                if (errorChannel == null) return;

                errorChannel.sendMessage(cc.getLocale(channel.getGuild().getLocale()).insufficientBotPerms).queue();
            } catch (Exception ignored) {
            }
        }
    }

    private void leaveChannel(AudioChannel channel) {
        if (!cc.db().isRegistered(channel)) return;

        Template template = cc.db().getTemplate(channel);
        if (template == null) return;

        VoiceChannel templateChannel = getChannel(template.getGuild(), template.getId());
        if (templateChannel == null) return;

        List<Clone> clones = cc.db().getClones(templateChannel);
        if (clones.isEmpty()) return;

        long occupiedChannels = clones.stream().filter(this::hasMembers).count() + (templateChannel.getMembers().isEmpty() ? 0 : 1);
        if (occupiedChannels >= clones.size()) return;

        Collections.sort(clones);
        Collections.reverse(clones);

        try {
            for (Clone clone : clones) {
                if (clone.getNumber() == 1) continue;

                VoiceChannel vc = getChannel(clone);
                if (vc != null && vc.getMembers().isEmpty()) {
                    vc.delete().queue();

                    if (clone.getSecondary() != null) {
                        VoiceChannel svc = getChannel(clone.getGuild(), clone.getSecondary());
                        if (svc != null) svc.delete().queue();
                    }

                    cc.db().removeClone(clone);
                    break;
                }
            }
        } catch (InsufficientPermissionException e) {
            try {
                TextChannel errorChannel = channel.getGuild().getTextChannelById(cc.db().getChannel(channel.getGuild()));
                if (errorChannel == null) return;

                errorChannel.sendMessage(cc.getLocale(channel.getGuild().getLocale()).insufficientBotPerms).queue();
            } catch (Exception ignored) {
            }
        }
    }

    private VoiceChannel getChannel(Clone clone) {
        if (clone.getGuild() == null) return null;

        Guild guild = cc.jda().getGuildById(clone.getGuild());
        if (guild == null) return null;

        return guild.getVoiceChannelById(clone.getId());
    }

    private VoiceChannel getChannel(String guild, String channel) {
        if (guild == null || channel == null) return null;

        Guild g = cc.jda().getGuildById(guild);
        if (g == null) return null;

        return g.getVoiceChannelById(channel);
    }

    private boolean hasMembers(Clone clone) {
        AudioChannel channel = getChannel(clone);
        if (channel == null) return false;

        return channel.getMembers().size() > 0;
    }

    private int getCloneNumber(List<Clone> clones) {
        for (int i = 2; i < Integer.MAX_VALUE; i++) {
            boolean found = false;

            for (Clone clone : clones) {
                if (clone.getNumber() == i) {
                    found = true;
                    break;
                }
            }

            if (!found) return i;
        }

        return -1;
    }
}
