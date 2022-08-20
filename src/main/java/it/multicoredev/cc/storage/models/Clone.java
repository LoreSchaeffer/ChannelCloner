package it.multicoredev.cc.storage.models;

import org.jetbrains.annotations.NotNull;

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
public class Clone implements Comparable<Clone> {
    private String id;
    private String guild;
    private String template;
    private Integer number;
    private String secondary;

    public Clone(String id, String guild, String template, Integer number, String secondary) {
        this.id = id;
        this.guild = guild;
        this.template = template;
        this.number = number;
        this.secondary = secondary;
    }

    public String getId() {
        if (id == null) return null;
        if (id.equals("null")) return null;
        return id;
    }

    public String getGuild() {
        if (guild == null) return null;
        if (guild.equals("null")) return null;
        return guild;
    }

    public String getTemplate() {
        if (template == null) return null;
        if (template.equals("null")) return null;
        return template;
    }

    public Integer getNumber() {
        return number;
    }

    public String getSecondary() {
        if (secondary == null) return null;
        if (secondary.equals("null")) return null;
        return secondary;
    }

    @Override
    public int compareTo(@NotNull Clone o) {
        return number.compareTo(o.number);
    }
}
