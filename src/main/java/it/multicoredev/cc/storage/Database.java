package it.multicoredev.cc.storage;

import it.multicoredev.cc.storage.models.Clone;
import it.multicoredev.cc.storage.models.SecondaryTemplate;
import it.multicoredev.cc.storage.models.Template;
import it.multicoredev.mclib.db.SQLite;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static it.multicoredev.cc.ChannelCloner.LOGGER;

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
public class Database {
    private static final String GUILDS = "guilds";
    private static final String TEMPLATES = "templates";
    private static final String SECONDARY_TEMPLATES = "secondary_templates";
    private static final String CLONES = "clones";
    private static final String SECONDARY_CLONES = "secondary_clones";

    private SQLite db;

    public Database(File file) throws SQLException {
        db = new SQLite(file);

        db.createTable(new String[]{
                "`guild` VARCHAR(20) PRIMARY KEY",
                "`enabled` TINYINT DEFAULT 1",
                "`channel` VARCHAR(20)",
        }, GUILDS);

        db.createTable(new String[]{
                "`id` VARCHAR(20) PRIMARY KEY",
                "`guild` VARCHAR(20) NOT NULL",
                "`name` VARCHAR(100) NOT NULL",
                "`secondary` VARCHAR(20)"
        }, TEMPLATES);

        db.createTable(new String[]{
                "`id` VARCHAR(20) PRIMARY KEY",
                "`guild` VARCHAR(20) NOT NULL",
                "`name` VARCHAR(100) NOT NULL",
                "`primary` VARCHAR(20)"
        }, SECONDARY_TEMPLATES);

        db.createTable(new String[]{
                "`id` VARCHAR(20) PRIMARY KEY",
                "`guild` VARCHAR(20) NOT NULL",
                "`template` VARCHAR(20) NOT NULL",
                "`number` INTEGER NOT NULL",
                "`secondary` VARCHAR(20)"
        }, CLONES);

        db.createTable(new String[]{
                "`id` VARCHAR(20) PRIMARY KEY",
                "`guild` VARCHAR(20) NOT NULL",
                "`number` INTEGER NOT NULL"
        }, SECONDARY_CLONES);
    }

    public Result enableBot(Guild guild, TextChannel channel) {
        if (guild == null) return new Result(false, "Guild is null");

        try {
            if (db.rowExists("guild", guild.getId(), GUILDS)) db.set(new String[]{"enabled", "channel"}, new Object[]{true, channel.getId()}, "guild", guild.getId(), GUILDS);
            else db.addRow(new String[]{"guild", "enabled", "channel"}, new Object[]{guild.getId(), true, channel.getId()}, GUILDS);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return new Result(false, e.getMessage());
        }

        return new Result(true, null);
    }

    public Result disableBot(Guild guild) {
        if (guild == null) return new Result(false, "Guild is null");

        try {
            if (db.rowExists("guild", guild.getId(), GUILDS)) db.set("enabled", false, "guild", guild.getId(), GUILDS);
            else db.addRow(new String[]{"guild", "enabled"}, new Object[]{guild.getId(), false}, GUILDS);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return new Result(false, e.getMessage());
        }

        return new Result(true, null);
    }

    public Result registerTemplate(AudioChannel channel, String name, AudioChannel secondary) {
        if (channel == null || name == null || name.trim().isEmpty()) return new Result(false, "Invalid parameters");

        try {
            db.addRow(
                    new String[]{"id", "guild", "name", "secondary"},
                    new Object[]{channel.getId(), channel.getGuild().getId(), name, secondary != null ? secondary.getId() : null},
                    TEMPLATES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return new Result(false, e.getMessage());
        }

        return new Result(true, null);
    }

    public Result registerSecondaryTemplate(AudioChannel primary, AudioChannel secondary, String name) {
        if (primary == null || secondary == null || name == null || name.trim().isEmpty()) return new Result(false, "Invalid parameters");

        try {
            db.addRow(
                    new String[]{"id", "guild", "name", "primary"},
                    new Object[]{secondary.getId(), secondary.getGuild().getId(), name, primary.getId()},
                    SECONDARY_TEMPLATES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return new Result(false, e.getMessage());
        }

        return new Result(true, null);
    }

    public Result unregisterTemplate(AudioChannel channel) {
        if (channel == null) return new Result(false, "Invalid parameters");

        try {
            if (db.rowExists(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, TEMPLATES)) {
                db.removeRow(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, TEMPLATES);
                return new Result(true, "primary");
            } else if (db.rowExists(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, SECONDARY_TEMPLATES)) {
                SecondaryTemplate template = db.getObject(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, SecondaryTemplate.class, SECONDARY_TEMPLATES);

                db.removeRow(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, SECONDARY_TEMPLATES);
                db.set("secondary", null, new String[]{"id", "guild"}, new Object[]{template.getPrimary(), template.getGuild()}, TEMPLATES);
                return new Result(true, "secondary");
            }
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return new Result(false, e.getMessage());
        }

        return new Result(true, null);
    }

    public Result addClone(AudioChannel clone, AudioChannel template, AudioChannel secondary, int number) {
        if (clone == null || template == null) return new Result(false, "Invalid parameters");

        try {
            db.addRow(
                    new String[]{"id", "guild", "template", "number", "secondary"},
                    new Object[]{clone.getId(), clone.getGuild().getId(), template.getId(), number, secondary != null ? secondary.getId() : null},
                    CLONES);
            if (secondary != null)
                db.addRow(
                        new String[]{"id", "guild", "number"},
                        new Object[]{secondary.getId(), secondary.getGuild().getId(), number},
                        SECONDARY_CLONES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return new Result(false, e.getMessage());
        }

        return new Result(true, null);
    }

    public Result removeClone(Clone clone) {
        if (clone == null) return new Result(false, "Invalid parameters");

        try {
            db.removeRow(new String[]{"id", "guild"}, new Object[]{clone.getId(), clone.getGuild()}, CLONES);
            if (clone.getSecondary() != null)
                db.removeRow(new String[]{"id", "guild"}, new Object[]{clone.getSecondary(), clone.getGuild()}, SECONDARY_CLONES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return new Result(false, e.getMessage());
        }

        return new Result(true, null);
    }

    public String getChannel(Guild guild) {
        if (guild == null) return null;

        try {
            if (db.rowExists("guild", guild.getId(), GUILDS))
                return db.getString("channel", "guild", guild.getId(), GUILDS);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }

        return null;
    }

    public boolean isEnabled(Guild guild) {
        if (guild == null) return false;

        try {
            if (db.rowExists("guild", guild.getId(), GUILDS))
                return db.getBoolean("guild", guild.getId(), "enabled", GUILDS);
            return false;
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return false;
        }
    }

    public boolean isRegistered(AudioChannel channel) {
        if (channel == null) return false;

        try {
            if (db.rowExists(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, TEMPLATES)) return true;
            return db.rowExists(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, CLONES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return false;
        }
    }

    public Template getTemplate(AudioChannel channel) {
        if (channel == null) return null;

        try {
            if (db.rowExists(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, TEMPLATES)) {
                return db.getObject(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, Template.class, TEMPLATES);
            } else {
                if (db.rowExists(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, CLONES)) {
                    String templateId = db.getString(new String[]{"id", "guild"}, new Object[]{channel.getId(), channel.getGuild().getId()}, "template", CLONES);
                    return getTemplate(templateId, channel.getGuild().getId());
                }
            }
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
        }

        return null;
    }

    public Template getTemplate(String id, String guild) {
        if (id == null || guild == null) return null;

        try {
            return db.getObject(new String[]{"id", "guild"}, new Object[]{id, guild}, Template.class, TEMPLATES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
        }

        return null;
    }

    public SecondaryTemplate getSecondaryTemplate(String guild, String id) {
        if (guild == null || id == null) return null;

        try {
            return db.getObject(new String[]{"id", "guild"}, new Object[]{id, guild}, SecondaryTemplate.class, SECONDARY_TEMPLATES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
        }

        return null;
    }

    public List<Clone> getClones(Template template) {
        if (template == null) return new ArrayList<>();

        try {
            return db.getList(new String[]{"guild", "template"}, new Object[]{template.getGuild(), template.getId()}, Clone.class, CLONES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
        }

        return new ArrayList<>();
    }

    public List<Clone> getClones(AudioChannel channel) {
        if (channel == null) return new ArrayList<>();

        try {
            return db.getList(new String[]{"guild", "template"}, new Object[]{channel.getGuild().getId(), channel.getId()}, Clone.class, CLONES);
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
        }

        return new ArrayList<>();
    }

    public static class Result {
        private final boolean success;
        private final String error;

        public Result(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }
}
