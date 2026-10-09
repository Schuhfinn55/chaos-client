package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.session.AccountSwitcher;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.Widget;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Account-Wechsel im Spiel: Accounts des Chaos Launchers (sofort wechseln), in anderen
 * Launchern gefundene Accounts (übernehmen oder per Microsoft anmelden) und ein
 * Microsoft-Login mit Code – alles ohne Neustart des Spiels.
 */
public class AccountSwitcherScreen extends ChaosScreen {

    private ScrollPanel list;
    private int px, py, pw, ph;
    private JsonObject data;
    private String status = "";
    private boolean statusError;
    private boolean loading = true, busy;
    private boolean needsRebuild;
    // Microsoft-Login
    private JsonObject login;
    private long lastPoll;
    private boolean polling;

    public AccountSwitcherScreen(Screen parent) {
        super("Accounts", parent);
        refresh();
    }

    private void refresh() {
        loading = true;
        AccountSwitcher.listAccounts(o -> {
            loading = false;
            if (o.has("error")) { data = null; setStatus(o.get("error").getAsString(), true); }
            else { data = o; }
            needsRebuild = true;
        });
    }

    private void setStatus(String s, boolean error) { status = s == null ? "" : s; statusError = error; }

    private void rebuild() { widgets.clear(); build(); }

    @Override
    protected void build() {
        pw = panelWidth(460);
        ph = panelHeight(400);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        add(new Button(px + pw - 136, py + 13, 68, 20, "Neu laden", this::refresh).style(Button.Style.GHOST)).enabled = !busy && login == null;
        list = add(new ScrollPanel(px + 8, py + 68, pw - 16, ph - 112));
        int y = list.y + 4, x = list.x + 6, w = list.w - 20;
        String own = client.getSession().getUuidOrNull() != null ? client.getSession().getUuidOrNull().toString().replace("-", "") : "";

        if (login != null) {
            y = buildLogin(x, y, w);
        } else if (loading) {
            list.add(new Label(x, y + 6, w, "Lade Accounts vom Chaos Launcher …", theme().textFaint()));
            y += 24;
        } else if (data == null) {
            list.add(new Label(x, y + 6, w, "Chaos Launcher nicht erreichbar.", theme().text()));
            list.add(new Label(x, y + 18, w, "Starte das Spiel über den Chaos Launcher, um Accounts zu wechseln.", theme().textFaint()));
            y += 40;
        } else {
            // Launcher-Accounts
            list.add(new Label(x, y, w, "CHAOS LAUNCHER", theme().accent()));
            y += 14;
            JsonArray accounts = data.getAsJsonArray("accounts");
            if (accounts == null || accounts.isEmpty()) {
                list.add(new Label(x, y + 2, w, "Noch kein Account im Launcher – unten per Microsoft anmelden.", theme().textFaint()));
                y += 20;
            }
            if (accounts != null) for (JsonElement e : accounts) {
                JsonObject a = e.getAsJsonObject();
                String uuid = str(a, "uuid"), name = str(a, "name");
                boolean current = uuid.equalsIgnoreCase(own);
                boolean canRefresh = a.has("canRefresh") && a.get("canRefresh").getAsBoolean();
                boolean valid = a.has("valid") && a.get("valid").getAsBoolean();
                list.add(new HeadWidget(x, y + 2, 22, uuid, name));
                list.add(new Label(x + 30, y + 3, w - 130, name + (current ? " §a● aktiv" : ""), current ? theme().accentLight() : theme().text()));
                list.add(new Label(x + 30, y + 14, w - 130, current ? "Diese Sitzung läuft gerade" : canRefresh ? "Chaos Launcher · Sitzung wird automatisch erneuert" : valid ? "Chaos Launcher · Token gültig" : "Chaos Launcher · Token abgelaufen, Anmeldung nötig", theme().textFaint()));
                if (current) {
                    list.add(new Button(x + w - 90, y + 3, 86, 20, "Aktiv", () -> {}).style(Button.Style.GHOST)).enabled = false;
                } else if (canRefresh || valid) {
                    Button b = list.add(new Button(x + w - 90, y + 3, 86, 20, "Wechseln", () -> switchTo(uuid, name)).style(Button.Style.PRIMARY));
                    b.enabled = !busy;
                } else {
                    list.add(new Button(x + w - 90, y + 3, 86, 20, "Anmelden", this::startLogin)).enabled = !busy;
                }
                y += 30;
            }
            // In anderen Launchern gefunden
            JsonArray foreign = data.getAsJsonArray("foreign");
            y += 8;
            list.add(new Label(x, y, w, "IN ANDEREN LAUNCHERN GEFUNDEN", theme().accent()));
            y += 14;
            if (foreign == null || foreign.isEmpty()) {
                list.add(new Label(x, y + 2, w, "Keine Accounts in anderen Launchern gefunden.", theme().textFaint()));
                list.add(new Label(x, y + 12, w, "Geprüft: Minecraft Launcher, Prism/PolyMC/MultiMC, Modrinth, NoRisk, ATLauncher, Lunar, TLauncher.", theme().textFaint()));
                y += 30;
            } else for (JsonElement e : foreign) {
                JsonObject f = e.getAsJsonObject();
                String uuid = str(f, "uuid"), name = str(f, "name"), source = str(f, "source"), sourceName = str(f, "sourceName");
                boolean hasToken = f.has("hasToken") && f.get("hasToken").getAsBoolean();
                boolean inLauncher = f.has("inLauncher") && f.get("inLauncher").getAsBoolean();
                boolean current = uuid.equalsIgnoreCase(own);
                list.add(new HeadWidget(x, y + 2, 22, uuid, name));
                list.add(new Label(x + 30, y + 3, w - 130, name + (current ? " §a● aktiv" : ""), theme().text()));
                String hint = sourceName + (inLauncher ? " · auch im Chaos Launcher" : hasToken ? " · Sitzung übernehmbar" : " · Microsoft-Login nötig");
                list.add(new Label(x + 30, y + 14, w - 130, Draw.trim(hint, w - 134), theme().textFaint()));
                if (current) {
                    list.add(new Button(x + w - 90, y + 3, 86, 20, "Aktiv", () -> {}).style(Button.Style.GHOST)).enabled = false;
                } else if (inLauncher) {
                    list.add(new Button(x + w - 90, y + 3, 86, 20, "Wechseln", () -> switchTo(uuid, name)).style(Button.Style.PRIMARY)).enabled = !busy;
                } else if (hasToken) {
                    list.add(new Button(x + w - 90, y + 3, 86, 20, "Übernehmen", () -> useForeign(source, uuid, name)).style(Button.Style.PRIMARY).tooltip("Sitzung aus " + sourceName + " übernehmen – ohne neue Anmeldung")).enabled = !busy;
                } else {
                    list.add(new Button(x + w - 90, y + 3, 86, 20, "Anmelden", this::startLogin).tooltip(sourceName + " speichert kein nutzbares Token – einmal per Microsoft anmelden")).enabled = !busy;
                }
                y += 30;
            }
        }
        list.setContentHeight(y + 8 - list.y);
        // Fußzeile
        if (login == null) {
            add(new Button(px + 12, py + ph - 34, 150, 22, "+ Microsoft-Login", this::startLogin).style(Button.Style.PRIMARY)).enabled = !busy && !loading && data != null;
        }
    }

    private int buildLogin(int x, int y, int w) {
        String code = str(login, "userCode"), uri = str(login, "verificationUri");
        list.add(new Label(x, y, w, "MICROSOFT-LOGIN", theme().accent()));
        y += 16;
        list.add(new Label(x, y, w, "1. Öffne im Browser:  §f" + uri, theme().textDim()));
        y += 12;
        list.add(new Label(x, y, w, "2. Gib diesen Code ein und melde dich mit deinem Microsoft-Konto an:", theme().textDim()));
        y += 16;
        list.add(new CodeWidget(x, y, w, code));
        y += 40;
        list.add(new Button(x, y, 110, 20, "Browser öffnen", () -> { try { Util.getOperatingSystem().open(URI.create(uri)); } catch (Exception ignored) {} }).style(Button.Style.PRIMARY));
        list.add(new Button(x + 116, y, 100, 20, "Code kopieren", () -> { client.keyboard.setClipboard(code); setStatus("Code kopiert.", false); }));
        list.add(new Button(x + 222, y, 80, 20, "Abbrechen", () -> { login = null; busy = false; setStatus("", false); needsRebuild = true; }).style(Button.Style.DANGER));
        y += 28;
        list.add(new Label(x, y, w, "Warte auf Bestätigung … der Account wird danach im Chaos Launcher gespeichert und sofort aktiv.", theme().textFaint()));
        return y + 16;
    }

    /* ---------- Aktionen ---------- */

    private void switchTo(String uuid, String name) {
        if (busy) return;
        busy = true;
        setStatus("Wechsle zu " + name + " …", false);
        needsRebuild = true;
        AccountSwitcher.switchTo(uuid, o -> {
            busy = false;
            if (o.has("error")) setStatus(o.get("error").getAsString(), true);
            else if (AccountSwitcher.applySession(o)) { setStatus("Angemeldet als " + name + ".", false); close(); return; }
            else setStatus("Sitzung konnte nicht gewechselt werden.", true);
            needsRebuild = true;
        });
    }

    private void useForeign(String source, String uuid, String name) {
        if (busy) return;
        busy = true;
        setStatus("Übernehme " + name + " …", false);
        needsRebuild = true;
        AccountSwitcher.useForeign(source, uuid, o -> {
            busy = false;
            if (o.has("error")) setStatus(o.get("error").getAsString(), true);
            else if (AccountSwitcher.applySession(o)) { close(); return; }
            else setStatus("Sitzung konnte nicht übernommen werden.", true);
            needsRebuild = true;
        });
    }

    private void startLogin() {
        if (busy) return;
        busy = true;
        setStatus("Starte Microsoft-Login …", false);
        needsRebuild = true;
        AccountSwitcher.loginStart(o -> {
            if (o.has("error")) { busy = false; setStatus(o.get("error").getAsString(), true); needsRebuild = true; return; }
            login = o;
            lastPoll = System.currentTimeMillis();
            setStatus("", false);
            try { Util.getOperatingSystem().open(URI.create(str(o, "verificationUri"))); } catch (Exception ignored) {}
            needsRebuild = true;
        });
    }

    private void pollLogin() {
        if (login == null || polling) return;
        polling = true;
        String code = str(login, "deviceCode");
        AccountSwitcher.loginStatus(code, o -> {
            polling = false;
            if (login == null) return;
            String st = str(o, "status");
            if ("done".equals(st)) {
                login = null;
                busy = false;
                if (AccountSwitcher.applySession(o)) { close(); return; }
                setStatus("Login abgeschlossen, Sitzung konnte aber nicht übernommen werden.", true);
                refresh();
            } else if ("error".equals(st) || "unknown".equals(st)) {
                login = null;
                busy = false;
                setStatus(o.has("error") ? o.get("error").getAsString() : "Login abgebrochen.", true);
                needsRebuild = true;
            }
        });
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        if (needsRebuild) { needsRebuild = false; rebuild(); }
        if (login != null && System.currentTimeMillis() - lastPoll > 2500) { lastPoll = System.currentTimeMillis(); pollLogin(); }
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "ACCOUNTS", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        String cur = "Angemeldet als §f" + client.getSession().getUsername() + " §8· §7Wechsel ohne Neustart über den Chaos Launcher";
        ctx.drawTextWithShadow(textRenderer, Draw.trim(cur, pw - 24), px + 12, py + 52, theme().textDim());
        if (!status.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, Draw.trim(status, pw - 190), px + 170, py + ph - 28, statusError ? 0xFFFF5555 : theme().textDim());
        }
    }

    private static String str(JsonObject o, String k) {
        return o != null && o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
    }

    /** Spielerkopf (Skin wird über den Skin-Provider geladen; bis dahin Standard-Skin). */
    private static final class HeadWidget extends Widget {
        private final Supplier<SkinTextures> skin;
        HeadWidget(int x, int y, int size, String uuid, String name) {
            super(x, y, size, size);
            Supplier<SkinTextures> s;
            try {
                UUID id = AccountSwitcher.toUuid(uuid);
                s = mc.getSkinProvider().supplySkinTextures(new GameProfile(id, name), false);
            } catch (Exception e) {
                s = null;
            }
            skin = s;
        }
        @Override
        public void render(DrawContext ctx, int mx, int my, float delta) {
            Draw.roundedRect(ctx, x - 1, y - 1, w + 2, h + 2, 4, theme().bg3());
            if (skin != null) {
                try { PlayerSkinDrawer.draw(ctx, skin.get(), x, y, w); return; } catch (Exception ignored) {}
            }
            ctx.fill(x, y, x + w, y + h, theme().bg2());
        }
    }

    /** Großer Anmelde-Code zum Abtippen. */
    private final class CodeWidget extends Widget {
        private final String code;
        CodeWidget(int x, int y, int w, String code) { super(x, y, w, 32); this.code = code; }
        @Override
        public void render(DrawContext ctx, int mx, int my, float delta) {
            Draw.roundedRect(ctx, x, y, w, h, 8, theme().bg2());
            Draw.roundedBorder(ctx, x, y, w, h, 8, theme().accent());
            var m = ctx.getMatrices();
            m.pushMatrix();
            m.translate(x + w / 2f, y + 7);
            m.scale(2f, 2f);
            ctx.drawCenteredTextWithShadow(textRenderer, code, 0, 0, 0xFFFFFFFF);
            m.popMatrix();
        }
    }
}
