package net.endercraftbuild.redis.commands;

import net.endercraftbuild.redis.RedisStats;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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

        final Player player = (Player)sender;

        player.sendMessage(ChatColor.RED + "===========" + ChatColor.GOLD + " Stats " + ChatColor.RED + "===========");

        new BukkitRunnable() {

            @Override
            public void run() {
                Jedis jedis = plugin.getJedisPool().getResource();
                Pipeline pipeline = jedis.pipelined();

                Response<String> killsResponse = pipeline.hget("uuid:" + player.getUniqueId().toString(), "kills");
                Response<String> deathsResponse = pipeline.hget("uuid:" + player.getUniqueId().toString(), "deaths");
                Response<String> joinsResponse = pipeline.hget("uuid:" + player.getUniqueId().toString(), "joins");
                Response<String> breaksResponse =  pipeline.hget("uuid:" + player.getUniqueId().toString(), "block-breaks");
                Response<String> placedResponse = pipeline.hget("uuid:" + player.getUniqueId().toString(), "blocks-placed");

                pipeline.sync(); //You need to read & close the pipeline before accessing the data below...

                String kills = killsResponse.get(); //Access data after pipeline is closed
                String deaths = deathsResponse.get();
                String joins = joinsResponse.get();
                String breaks = breaksResponse.get();
                String placed = placedResponse.get();

                //All Done with jedis, will just display stats in chat async
                plugin.getJedisPool().returnResource(jedis);

                //Kills + Deaths
                if(plugin.getConfig().getBoolean("kill-death-stats")) {


                    player.sendMessage(ChatColor.GOLD + "Kills: " + ChatColor.YELLOW + kills);
                    player.sendMessage(ChatColor.GOLD + "Deaths: " + ChatColor.YELLOW + deaths);

                    if(kills != null && deaths != null) {
                        player.sendMessage(ChatColor.GOLD + "K/D Ratio: " + ChatColor.YELLOW + Double.valueOf(kills) / Double.valueOf(deaths));
                    }


                }
                //Joins
                if(plugin.getConfig().getBoolean("join-stats")) {

                    player.sendMessage(ChatColor.GOLD + "Server Joins: " + ChatColor.YELLOW + joins);

                }

                //Block breaks
                if(plugin.getConfig().getBoolean("block-breaks")) {

                    player.sendMessage(ChatColor.GOLD + "Blocks broken: " + ChatColor.YELLOW + breaks);

                }

                //Blocks placed
                if(plugin.getConfig().getBoolean("blocks-placed")) {

                    player.sendMessage(ChatColor.GOLD + "Blocks placed: " + ChatColor.YELLOW + placed);

                }

                player.sendMessage(ChatColor.RED + "============================");
            }

        }.runTaskAsynchronously(plugin);


        return true;

    }

}
