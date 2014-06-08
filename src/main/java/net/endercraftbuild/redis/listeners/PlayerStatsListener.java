package net.endercraftbuild.redis.listeners;

import net.endercraftbuild.redis.RedisStats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import redis.clients.jedis.Jedis;

public class PlayerStatsListener implements Listener {

    private RedisStats plugin;

    public PlayerStatsListener(RedisStats plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        if(!(plugin.getConfig().getBoolean("kill-death-stats")))
            return;

        Player died = event.getEntity();

        Jedis jedis = plugin.getJedisPool().getResource();

        //Increment Player deaths by 1 - This is a Redis Hash
        jedis.hincrBy("uuid:" + died.getUniqueId().toString(), "deaths", 1);

        if(event.getEntity().getKiller() instanceof Player) {
            //Increment Killer kills by 1
            jedis.hincrBy("uuid:" + event.getEntity().getKiller().getUniqueId().toString(), "kills", 1);
        }

        plugin.getJedisPool().returnResource(jedis); //Return the connection to the pool

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!(plugin.getConfig().getBoolean("join-stats")))
            return;

        Player player = event.getPlayer();

        Jedis jedis = plugin.getJedisPool().getResource();

        jedis.hincrBy("uuid:" + player.getUniqueId().toString(), "joins", 1);

        plugin.getJedisPool().returnResource(jedis); //Return to pool

    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if(!(plugin.getConfig().getBoolean("block-breaks")))
            return;

        Player player = event.getPlayer();

        Jedis jedis = plugin.getJedisPool().getResource();
        //This will get called a lot... Redis can handle a lot :)
        jedis.hincrBy("uuid:" + player.getUniqueId().toString(), "block-breaks", 1);

        plugin.getJedisPool().returnResource(jedis);

    }

}
