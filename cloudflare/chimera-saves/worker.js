/**
 * Chimera Cloud Save Worker
 * ─────────────────────────
 * Routes:
 *   POST   /save          — upsert a save slot
 *   GET    /save/:slotId  — fetch a save slot
 *   DELETE /save/:slotId  — wipe a save slot
 *
 * Auth: Authorization: Bearer <API_TOKEN>
 * Binding: env.DB → D1 database "chimera-saves"
 */

const JSON_HEADERS = { "Content-Type": "application/json" };

function json(data, status = 200) {
  return new Response(JSON.stringify(data), { status, headers: JSON_HEADERS });
}

function err(msg, status) {
  return json({ error: msg }, status);
}

function authorize(request, env) {
  const header = request.headers.get("Authorization") ?? "";
  const token  = header.startsWith("Bearer ") ? header.slice(7) : null;
  return token && token === env.API_TOKEN;
}

export default {
  async fetch(request, env) {
    if (!authorize(request, env)) return err("Unauthorized", 401);

    const url    = new URL(request.url);
    const parts  = url.pathname.replace(/^\/+|\/+$/g, "").split("/");
    const [base, rawSlotId] = parts;

    if (base !== "save") return err("Not found", 404);

    // ── POST /save ────────────────────────────────────────────────────────────
    if (request.method === "POST" && !rawSlotId) {
      let body;
      try { body = await request.json(); }
      catch { return err("Invalid JSON body", 400); }

      const { slot_id, player_name, chapter_tag, playtime_seconds, save_data_json } = body;
      if (slot_id == null) return err("slot_id required", 400);

      const now = Date.now();
      await env.DB.prepare(`
        INSERT INTO saves (slot_id, player_name, chapter_tag, playtime_seconds, save_data_json, updated_at)
        VALUES (?, ?, ?, ?, ?, ?)
        ON CONFLICT(slot_id) DO UPDATE SET
          player_name      = excluded.player_name,
          chapter_tag      = excluded.chapter_tag,
          playtime_seconds = excluded.playtime_seconds,
          save_data_json   = excluded.save_data_json,
          updated_at       = excluded.updated_at
      `).bind(
        slot_id,
        player_name      ?? "",
        chapter_tag      ?? "prologue",
        playtime_seconds ?? 0,
        typeof save_data_json === "string" ? save_data_json : JSON.stringify(save_data_json ?? {}),
        now
      ).run();

      return json({ ok: true, slot_id, updated_at: now });
    }

    // ── GET /save/:slotId ─────────────────────────────────────────────────────
    if (request.method === "GET" && rawSlotId) {
      const slotId = parseInt(rawSlotId, 10);
      if (isNaN(slotId)) return err("Invalid slot_id", 400);

      const { results } = await env.DB.prepare(
        "SELECT * FROM saves WHERE slot_id = ?"
      ).bind(slotId).all();

      if (!results.length) return json(null, 404);
      return json(results[0]);
    }

    // ── DELETE /save/:slotId ──────────────────────────────────────────────────
    if (request.method === "DELETE" && rawSlotId) {
      const slotId = parseInt(rawSlotId, 10);
      if (isNaN(slotId)) return err("Invalid slot_id", 400);

      await env.DB.prepare("DELETE FROM saves WHERE slot_id = ?").bind(slotId).run();
      return json({ ok: true, deleted: slotId });
    }

    return err("Method not allowed", 405);
  },
};
