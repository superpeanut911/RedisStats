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

import java.util.logging.Level;

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

                Long startedTime = System.currentTimeMillis(); //Lets see how fast this is

                Response<String> killsResponse = pipeline.hget("uuid:" + player.getUniqueId().toString(), "kills");
                Response<String> deathsResponse = pipeline.hget("uuid:" + player.getUniqueId().toString(), "deaths");
                Response<String> joinsResponse = pipeline.hget("uuid:" + player.getUniqueId().toString(), "joins");
                Response<String> breaksResponse =  pipeline.hget("uuid:" + player.getUniqueId().toString(), "block-breaks");
                Response<String> placedResponse = pipeline.hget("uuid:" + player.getUniqueId().toString(), "blocks-placed");
                //Global stats
                Response<String> globalKillsRes = pipeline.hget("global", "kills");
                Response<String> globalJoinsRes = pipeline.hget("global", "joins");
                Response<String> globalDeathsRes = pipeline.hget("global", "deaths");
                Response<String> globalBlocksPlacedRes = pipeline.hget("global" , "blocks-placed");
                Response<String> globalBlocksBrokeRes = pipeline.hget("global", "block-breaks");
                Response<String> globalPlayRes = pipeline.hget("global", "time-played");

                pipeline.sync(); //You need to read & close the pipeline before accessing the data below...

                String kills = killsResponse.get(); //Access data after pipeline is closed
                String deaths = deathsResponse.get();
                String joins = joinsResponse.get();
                String breaks = breaksResponse.get();
                String placed = placedResponse.get();
                String globalKills = globalKillsRes.get();
                String globalDeaths = globalDeathsRes.get();
                String globalJoins = globalJoinsRes.get();
                String globalBlocksPlaced = globalBlocksPlacedRes.get();
                String globalBlocksBroke = globalBlocksBrokeRes.get();
                String globalPlayTime = globalPlayRes.get();

                //All Done with jedis, will just display stats in chat async
                plugin.getJedisPool().returnResource(jedis);

                Long endedTime = System.currentTimeMillis();

                plugin.getServer().getLogger().log(Level.INFO, "[RedisStats] Got stats (" + player.getName() + ") in " + (endedTime - startedTime) + " milliseconds");

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
                player.sendMessage(ChatColor.RED + "========" + ChatColor.YELLOW + " Global Stats " + ChatColor.RED + "========");
                String time = plugin.convertMillisToDays(Long.valueOf(globalPlayTime)) + " days"; //Format to days
                player.sendMessage(ChatColor.GOLD + "Time played: " + ChatColor.YELLOW + time);
                player.sendMessage(ChatColor.GOLD + "Global Kills: " + ChatColor.YELLOW + globalKills);
                player.sendMessage(ChatColor.GOLD + "Global Deaths: " + ChatColor.YELLOW + globalDeaths);
                player.sendMessage(ChatColor.GOLD + "Global K/D: " + ChatColor.YELLOW + Double.valueOf(globalKills) / Double.valueOf(globalDeaths));
                player.sendMessage(ChatColor.GOLD + "Global Joins: " + ChatColor.YELLOW + globalJoins );
                player.sendMessage(ChatColor.GOLD + "Global Blocks Broken: " + ChatColor.YELLOW + globalBlocksBroke);
                player.sendMessage(ChatColor.GOLD + "Global Blocks Placed: " + ChatColor.YELLOW + globalBlocksPlaced);
                player.sendMessage(ChatColor.DARK_GRAY + ChatColor.ITALIC.toString() + "Got all data in: " + (endedTime - startedTime) + "ms"); //Easily keep track
                player.sendMessage(ChatColor.RED + "============================");
            }

        }.runTaskAsynchronously(plugin);


        return true;

    }

}
