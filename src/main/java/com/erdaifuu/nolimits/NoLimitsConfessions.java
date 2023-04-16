package com.erdaifuu.nolimits;

import com.erdaifuu.nolimits.commands.CommandManager;
import com.erdaifuu.nolimits.listeners.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;

public class NoLimitsConfessions {
    private final ShardManager shardManager;

    public NoLimitsConfessions() throws LoginException, InterruptedException, SQLException {
        SQLiteDataSource.getConnection();

        final String TOKEN = "";
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(TOKEN);

        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("/confessnl"));
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setChunkingFilter(ChunkingFilter.ALL);

        shardManager = builder.build();

        // Register listeners
        shardManager.addEventListener(new EventListener(), new CommandManager());
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public JDA getJDA() {
        final String TOKEN = "";
        JDABuilder jdaBuilder = JDABuilder.createDefault(TOKEN);
        JDA jda = jdaBuilder.build();
        return jda;
    }

    public static void main(String[] args){
        try {
            NoLimitsConfessions bot = new NoLimitsConfessions();
        } catch (LoginException | InterruptedException | SQLException e) {
            System.out.println("ERROR: Provided bot token is invalid!");
        }
    }
}
