package com.erdaifuu.nolimits.commands;

import com.erdaifuu.nolimits.NoLimitsConfessions;
import com.erdaifuu.nolimits.SQLiteDataSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.awt.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("confessnl")) {
            JDA jda = event.getJDA();

            Guild logGuild = null;
            try {
                logGuild = jda.awaitReady().getGuildById("1095825768824713258");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            long guildId = event.getGuild().getIdLong();
            int counter = getCounter(guildId);

            event.deferReply().setEphemeral(true).queue();

            // Create the mappings for the confession command
            OptionMapping confessionOption = event.getOption("confession");
            OptionMapping confessionAttachment = event.getOption("attachment");
            String confession = confessionOption.getAsString();

            // Send the confessions to the logs channel
            TextChannel txtChannel = logGuild.getTextChannelById("");
            String userTag = event.getUser().getAsTag();
            String serverTag = event.getGuild().getName();
            String serverChannel = event.getGuildChannel().getName();

            // Build the embed for that
            EmbedBuilder logEmbed = new EmbedBuilder();
            logEmbed.setDescription(confession);
            logEmbed.setTitle("Logged Confession - " + userTag);
            logEmbed.setColor(new Color(255, 0, 0));
            logEmbed.setFooter(serverTag + " (#" + counter + ")" + "\n#" + serverChannel);

            assert txtChannel != null;
            txtChannel.sendMessageEmbeds(logEmbed.build()).queue();

            // Build the embeds for the message itself
            EmbedBuilder confessionEmbed = new EmbedBuilder();
            if(confessionAttachment != null){
                Message.Attachment attachment = confessionAttachment.getAsAttachment();
                confessionEmbed.setImage(attachment.getUrl());
            }
            confessionEmbed.setDescription(confession);
            confessionEmbed.setTitle("Anonymous Confession (#" + counter + ")");
            confessionEmbed.setColor(new Color((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)));
            confessionEmbed.setFooter("❗ If this confession is ToS-breaking or overtly hateful, you can report it using \"/report " + counter + "\"\n");

            sendConfirmMessage(event);
            event.getChannel().sendMessageEmbeds(confessionEmbed.build()).queue();

            incrementCounter(guildId);

        } else if (command.equals("report")) {
            OptionMapping reportNum = event.getOption("confessionnumber");
            assert reportNum != null;
            String id = reportNum.getAsString();
            Float ID = Float.parseFloat(id);
            float IDD = ID;
            int IDDD = (int)IDD;
            String userMention = event.getUser().getAsMention();
            event.deferReply().queue();
            event.getHook().sendMessage(userMention + "just tried to report confession #" + IDDD + ".").queue();
        } else if (command.equals("help")) {
            event.reply("Try /confessnl to send a confession.").queue();
        }
    }

    // Guild commands
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        // Command: /say message
        OptionData userConfession = new OptionData(OptionType.STRING, "confession", "Something you want to tell the world but are too afraid to", true);
        OptionData userConfessionAttachment = new OptionData(OptionType.ATTACHMENT, "attachment", "Optional media", false);
        commandData.add(Commands.slash("confessnl", "Confess your deepest darkest secrets.").addOptions(userConfession, userConfessionAttachment));

        OptionData confessionNumber = new OptionData(OptionType.NUMBER, "confessionnumber", "ID of confession that you wanted to report", true);
        commandData.add(Commands.slash("report", "Report a confession for breaking the rules.").addOptions(confessionNumber));
        commandData.add(Commands.slash("help", "Help on using the bot."));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    public void sendConfirmMessage(SlashCommandInteractionEvent event) {
        event.getHook().sendMessage("Your confession has been successfully sent!").queue(null, (exception) -> {
            System.out.println("There was an error 1.");
            event.getHook().sendMessage("There was an error " + exception).queue();
        });
    }

    public void replyHelp(SlashCommandInteractionEvent event) {
        event.reply("Try /confessnl to send a confession.").queue(null, (exception) -> {
            System.out.println("There was an error 2.");
            event.getHook().sendMessage("There was an error " + exception).queue();
        });
    }

    public void sendMessage(User user, String content) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue();
    }

    private int getCounter(long guildId){
        try (final PreparedStatement preparedStatement = SQLiteDataSource
                .getConnection()
                // language=SQLite
                .prepareStatement("SELECT confession_counter FROM guild_settings WHERE guild_id = ?")) {

            preparedStatement.setString(1, String.valueOf(guildId));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("confession_counter");
                }
            }

            try (final PreparedStatement insertStatement = SQLiteDataSource
                    .getConnection()
                    // language=SQLite
                    .prepareStatement("INSERT INTO guild_settings(guild_id) VALUES(?)")) {

                insertStatement.setString(1, String.valueOf(guildId));

                insertStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 1;
    }

    private void incrementCounter(long guildId) {
        try (final PreparedStatement preparedStatement = SQLiteDataSource
                .getConnection()
                // language=SQLite
                .prepareStatement("UPDATE guild_settings SET confession_counter = confession_counter + 1 WHERE guild_id = ?")) {

            preparedStatement.setString(1, String.valueOf(guildId));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("report", "Report a confession."));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    // Global commands
    @Override
    public void onReady(ReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("report", "Report a confession."));
        event.getJDA().updateCommands().addCommands(CommandData).queue();
    }
     */
}
