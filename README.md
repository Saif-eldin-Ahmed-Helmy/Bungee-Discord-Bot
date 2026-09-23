# Bungee Discord Bot

A Java Discord/BungeeCord integration built with **Javacord** and the **BungeeCord API**.

## Configuration

The bot reads `DISCORD_BOT_TOKEN`, `BUNGEE_DB_HOST`, `BUNGEE_DB_PORT`, `BUNGEE_DB_USER`, `BUNGEE_DB_PASSWORD`, and `BUNGEE_DB_NAME` from its process environment. Coupon creation also requires `TEBEX_SECRET`. Transcript upload is attempted only when a **separate** `TRANSCRIPT_API_TOKEN` is present. The transcript service uses its own token.

Configure Discord/server IDs and service endpoints for your deployment. Build with a JDK compatible with the included Gradle 7.1.1 wrapper. Transcript uploads integrate with an external archive service.

It links your Minecraft Bungee network to your Discord server, handles tickets and staff logs, adds custom commands and coupon creation, and protects your community with captcha verification.

---

## ✅ Features

- Connects Minecraft players and Discord in real time  
- Ticket system using **MySQL**; optional external transcript upload
- Utility commands like `/avatar`, `/clear`, and `/embed`  
- Coupon creation using **Tebex API** and user balance  
- Captcha verification when users join Discord  
- Staff logs to monitor and moderate activity  

---

## 🖼️ Screenshots

> **Images**

### 🖼️ UI Screens

![Page](https://i.imgur.com/rQruif8.png)  
![Page](https://i.imgur.com/66dogHm.png)  
![Page](https://i.imgur.com/hixy9PW.png)  
![Page](https://i.imgur.com/0tO02JR.png)  
![Page](https://i.imgur.com/PWcPqxs.png)  
![Page](https://i.imgur.com/W393ZvP.png)  
![Page](https://i.imgur.com/iABWodi.png)  
![Page](https://i.imgur.com/IZ2xs2p.png)  
![Page](https://i.imgur.com/Q3Xi871.png)  
![Page](https://i.imgur.com/d07VW7r.png)  
![Page](https://i.imgur.com/Z9N7pac.png)  
![Page](https://i.imgur.com/GK3zdKg.png)  

---

## 🛠️ Tech Stack

- **Language**: Java  
- **Discord API**: Javacord
- **Minecraft API**: BungeeCord Plugin API  
- **Database**: MySQL  
- **Web Archive**: External transcript archive integration
- **Payments**: Tebex API  
- **Captcha**: Image-based verification

---
