/**
 * Cloudflare Worker Gateway for CycleJournal Zero-Knowledge Backup Vault
 */
export interface Env {
  DB: D1Database;
  API_SECRET_KEY?: string;
}

interface BackupPayload {
  user_id: string;
  backup_month: string;
  cipher_payload: string;
  payload_hash: string;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    // Optional API key protection
    if (env.API_SECRET_KEY) {
      const authKey = request.headers.get("x-api-key");
      if (authKey !== env.API_SECRET_KEY) {
        return new Response(JSON.stringify({ error: "Unauthorized" }), {
          status: 401,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    // Router
    if (url.pathname === "/api/backup") {
      switch (request.method) {
        case "POST":
          return handleUpload(request, env);
        case "GET":
          return handleGet(url, env);
        case "DELETE":
          return handleDelete(url, env);
        default:
          return new Response(JSON.stringify({ error: "Method not allowed" }), { status: 405 });
      }
    }

    return new Response(JSON.stringify({ error: "Not Found" }), { status: 404 });
  }
};

async function handleUpload(request: Request, env: Env): Promise<Response> {
  try {
    const body = (await request.json()) as BackupPayload;
    if (!body.user_id || !body.backup_month || !body.cipher_payload || !body.payload_hash) {
      return new Response(JSON.stringify({ error: "Missing required fields" }), { status: 400 });
    }

    if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(body.backup_month)) {
      return new Response(JSON.stringify({ error: "Invalid backup_month format. Use YYYY-MM" }), { status: 400 });
    }

    // Verify SHA-256 hash
    const dataBuffer = new TextEncoder().encode(body.cipher_payload);
    const digestBuffer = await crypto.subtle.digest("SHA-256", dataBuffer);
    const computedHash = Array.from(new Uint8Array(digestBuffer))
      .map((b) => b.toString(16).padStart(2, "0"))
      .join("");

    if (computedHash.toLowerCase() !== body.payload_hash.toLowerCase()) {
      return new Response(JSON.stringify({ error: "Payload integrity check failed (SHA256 mismatch)" }), { status: 400 });
    }

    const now = Date.now();
    const query = `
      INSERT INTO monthly_backups (user_id, backup_month, cipher_payload, payload_hash, created_at, updated_at)
      VALUES (?, ?, ?, ?, ?, ?)
      ON CONFLICT(user_id, backup_month) DO UPDATE SET
        cipher_payload = excluded.cipher_payload,
        payload_hash = excluded.payload_hash,
        updated_at = excluded.updated_at
    `;

    await env.DB.prepare(query)
      .bind(body.user_id, body.backup_month, body.cipher_payload, body.payload_hash, now, now)
      .run();

    return new Response(JSON.stringify({ success: true, message: `Backup for ${body.backup_month} synchronized` }), {
      status: 200,
      headers: { "Content-Type": "application/json" }
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : "Internal error";
    return new Response(JSON.stringify({ error: message }), { status: 500 });
  }
}

async function handleGet(url: URL, env: Env): Promise<Response> {
  const userId = url.searchParams.get("user_id");
  const month = url.searchParams.get("backup_month");

  if (!userId) {
    return new Response(JSON.stringify({ error: "user_id is required" }), { status: 400 });
  }

  try {
    if (month) {
      const result = await env.DB.prepare(
        "SELECT user_id, backup_month, cipher_payload, payload_hash, updated_at FROM monthly_backups WHERE user_id = ? AND backup_month = ?"
      )
        .bind(userId, month)
        .first();

      if (!result) {
        return new Response(JSON.stringify({ error: "Backup not found" }), { status: 404 });
      }

      return new Response(JSON.stringify({ success: true, data: result }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    } else {
      const { results } = await env.DB.prepare(
        "SELECT backup_month, payload_hash, updated_at FROM monthly_backups WHERE user_id = ? ORDER BY backup_month DESC"
      )
        .bind(userId)
        .all();

      return new Response(JSON.stringify({ success: true, available_backups: results }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    }
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : "Database query error";
    return new Response(JSON.stringify({ error: message }), { status: 500 });
  }
}

async function handleDelete(url: URL, env: Env): Promise<Response> {
  const userId = url.searchParams.get("user_id");
  if (!userId) {
    return new Response(JSON.stringify({ error: "user_id is required" }), { status: 400 });
  }

  try {
    const result = await env.DB.prepare("DELETE FROM monthly_backups WHERE user_id = ?")
      .bind(userId)
      .run();

    return new Response(JSON.stringify({ success: true, rows_deleted: result.meta.changes }), {
      status: 200,
      headers: { "Content-Type": "application/json" }
    });
  } catch (err: unknown) {
    const message = err instanceof Error ? err.message : "Delete error";
    return new Response(JSON.stringify({ error: message }), { status: 500 });
  }
}
