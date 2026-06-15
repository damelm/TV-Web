package com.tv.webview;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Pantalla de inicio: lista de direcciones guardadas con anadir / editar / borrar. */
public class MainActivity extends AppCompatActivity implements BookmarkAdapter.Listener {

    private BookmarkStore store;
    private List<Bookmark> items;
    private BookmarkAdapter adapter;
    private RecyclerView recycler;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        store = new BookmarkStore(this);
        items = store.load();

        recycler = findViewById(R.id.recycler);
        empty = findViewById(R.id.tvEmpty);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookmarkAdapter(items, this);
        recycler.setAdapter(adapter);

        Button add = findViewById(R.id.btnAdd);
        add.setOnClickListener(v -> showEditDialog(-1));

        refreshEmpty();
    }

    private void refreshEmpty() {
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ----- Acciones de la lista -----

    @Override
    public void onOpen(int position) {
        Bookmark b = items.get(position);
        Intent i = new Intent(this, BrowserActivity.class);
        i.putExtra(BrowserActivity.EXTRA_URL, b.url);
        startActivity(i);
    }

    @Override
    public void onMenu(int position) {
        Bookmark b = items.get(position);
        new AlertDialog.Builder(this)
                .setTitle(b.name)
                .setItems(new CharSequence[]{
                        getString(R.string.action_open),
                        getString(R.string.action_edit),
                        getString(R.string.action_delete)
                }, (dialog, which) -> {
                    if (which == 0) {
                        onOpen(position);
                    } else if (which == 1) {
                        showEditDialog(position);
                    } else {
                        confirmDelete(position);
                    }
                })
                .show();
    }

    private void confirmDelete(int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_delete)
                .setMessage(getString(R.string.confirm_delete, items.get(position).name))
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    items.remove(position);
                    store.save(items);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, items.size());
                    refreshEmpty();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // position == -1 -> anadir nuevo; si no, editar el existente
    private void showEditDialog(int position) {
        boolean editing = position >= 0;
        View form = getLayoutInflater().inflate(R.layout.dialog_edit, null);
        EditText etName = form.findViewById(R.id.etName);
        EditText etUrl = form.findViewById(R.id.etUrl);

        if (editing) {
            Bookmark b = items.get(position);
            etName.setText(b.name);
            etUrl.setText(b.url);
        }

        new AlertDialog.Builder(this)
                .setTitle(editing ? R.string.title_edit : R.string.title_add)
                .setView(form)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String url = normalizeUrl(etUrl.getText().toString().trim());
                    if (TextUtils.isEmpty(url)) return;
                    if (TextUtils.isEmpty(name)) name = url;

                    if (editing) {
                        items.get(position).name = name;
                        items.get(position).url = url;
                        adapter.notifyItemChanged(position);
                    } else {
                        items.add(new Bookmark(name, url));
                        adapter.notifyItemInserted(items.size() - 1);
                    }
                    store.save(items);
                    refreshEmpty();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // Anade https:// si el usuario no escribio el esquema
    private String normalizeUrl(String input) {
        if (TextUtils.isEmpty(input)) return "";
        if (!input.matches("(?i)^[a-z]+://.*")) {
            return "https://" + input;
        }
        return input;
    }
}
