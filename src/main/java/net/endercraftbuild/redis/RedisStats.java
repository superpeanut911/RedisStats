package net.endercraftbuild.redis;

import net.endercraftbuild.redis.commands.StatsCommand;
import net.endercraftbuild.redis.listeners.PlayerStatsListener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;

public class RedisStats extends JavaPlugin {

    private JedisPool pool = null;
    public void onEnable() {

        File configFile = new File(getDataFolder() + "/config.yml");

        if (!configFile.exists()) {
            this.saveDefaultConfig();
        }
        this.getConfig().options().copyDefaults(true);

        try {
            pool = new JedisPool(new JedisPoolConfig(), this.getConfig().getString("redis-server"), this.getConfig().getInt("redis-port"));

            Jedis jedis = getJedisPool().getResource();
            jedis.ping(); //You can use ping to make sure it is properly connected!
            getJedisPool().returnResource(jedis);

        } catch (JedisConnectionException e) {
            System.out.println("[RedisStats] Jedis Connection issue: " + e + " - SHUTTING DOWN PLUGIN TO PREVENT FURTHER ISSUES!");
            getServer().getPluginManager().disablePlugin(this);
        }

        //If jedis connection fails and plugin gets disabled, registering will error
        if(this.isEnabled()) {
            //Register Commands
            getCommand("stats").setExecutor(new StatsCommand(this));
            //Register Listener
            this.getServer().getPluginManager().registerEvents(new PlayerStatsListener(this), this);
        }
    }
    public void onDisable() {
        getJedisPool().destroy();
    }

    public JedisPool getJedisPool() {
        return pool;
    }

    public float convertMillisToMinutes(Long l) {
        return (l / 1000) / 60;
    }
    public float convertMillisToHours(Long l) {
        return convertMillisToMinutes(l) / 60;
    }
    public float convertMillisToDays(Long l) {
        return convertMillisToHours(l) / 24;
    }
}
