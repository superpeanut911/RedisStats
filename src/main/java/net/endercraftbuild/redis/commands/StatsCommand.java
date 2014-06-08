package net.endercraftbuild.redis.commands;

import net.endercraftbuild.redis.RedisStats;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class StatsCommand implements CommandExecutor {

    private RedisStats plugin;

    public StatsCommand(RedisStats plugin) {
        this.plugin = plugin;
    }
    // /stats
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player");
            return true;
        }

        Player player = (Player)sender;

        player.sendMessage(ChatColor.RED + "===========" + ChatColor.GOLD + " Stats " + ChatColor.RED + "===========");

        //Kills + Deaths
        if(plugin.getConfig().getBoolean("kill-death-stats")) {

            Jedis jedisKD = plugin.getJedisPool().getResource();

            //Pipeline Example. It won't see much performance difference with only 2 requests but with many requests it can
            Pipeline pipelineKD = jedisKD.pipelined();

            Response<String> killsResonse = pipelineKD.hget("uuid:" + player.getUniqueId().toString(), "kills");
            Response<String> deathsResponse = pipelineKD.hget("uuid:" + player.getUniqueId().toString(), "deaths");

            pipelineKD.sync(); //You need to read & close the pipeline before accessing the data below...

            String kills = killsResonse.get(); //Access data after pipeline is closed
            String deaths = deathsResponse.get();

            player.sendMessage(ChatColor.GOLD + "Kills: " + ChatColor.YELLOW + kills);
            player.sendMessage(ChatColor.GOLD + "Deaths: " + ChatColor.YELLOW + deaths);

            if(kills != null && deaths != null) {
                player.sendMessage(ChatColor.GOLD + "K/D Ratio: " + ChatColor.YELLOW + Double.valueOf(kills) / Double.valueOf(deaths));
            }

            plugin.getJedisPool().returnResource(jedisKD); //Return to pool
        }
        //Joins
        if(plugin.getConfig().getBoolean("join-stats")) {
            //Only 1 request, no need to pipeline this
            Jedis jedisJ = plugin.getJedisPool().getResource();

            String joins= jedisJ.hget("uuid:" + player.getUniqueId().toString(), "joins");

            player.sendMessage(ChatColor.GOLD + "Server Joins: " + ChatColor.YELLOW + joins);

            plugin.getJedisPool().returnResource(jedisJ); //Massive mem leak if you dont do this

        }
        //Block breaks
        if(plugin.getConfig().getBoolean("block-breaks")) {
            Jedis jedisB = plugin.getJedisPool().getResource();

            String breaks = jedisB.hget("uuid:" + player.getUniqueId().toString(), "block-breaks");

            player.sendMessage(ChatColor.GOLD + "Blocks broken: " + ChatColor.YELLOW + breaks);

            plugin.getJedisPool().returnResource(jedisB);

        }

        player.sendMessage(ChatColor.RED + "============================");
        return true;

    }

}
