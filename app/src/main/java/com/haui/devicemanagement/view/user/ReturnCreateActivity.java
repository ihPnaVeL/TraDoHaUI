package com.haui.devicemanagement.view.user;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.haui.devicemanagement.R;
import com.haui.devicemanagement.data.DatabaseHelper;
import com.haui.devicemanagement.data.entity.BorrowItem;
import com.haui.devicemanagement.data.entity.BorrowTicket;
import com.haui.devicemanagement.presenter.BorrowPresenter;
import com.haui.devicemanagement.presenter.ReturnPresenter;
import com.haui.devicemanagement.util.Constants;
import com.haui.devicemanagement.util.SessionManager;
import com.haui.devicemanagement.view.adapter.BorrowItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class ReturnCreateActivity extends AppCompatActivity {

    private Spinner spinnerBorrowTickets;
    private CardView cardDevices;
    private RecyclerView rvReturnDevices;
    private TextInputEditText etReturnNote;
    private MaterialButton btnSubmitReturn;

    private SessionManager session;
    private BorrowPresenter borrowPresenter;
    private ReturnPresenter returnPresenter;

    private List<BorrowTicket> activeTickets = new ArrayList<>();
    private BorrowItemAdapter borrowItemAdapter;
    private int selectedBorrowTicketId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_create);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tạo phiếu trả");
        }

        session = new SessionManager(this);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        borrowPresenter = new BorrowPresenter(dbHelper);
        returnPresenter = new ReturnPresenter(dbHelper);

        initViews();
        setupRecyclerView();
        loadActiveTickets();
        applyDarkTheme();
    }

    private void initViews() {
        spinnerBorrowTickets = findViewById(R.id.spinnerBorrowTickets);
        cardDevices = findViewById(R.id.cardDevices);
        rvReturnDevices = findViewById(R.id.rvReturnDevices);
        etReturnNote = findViewById(R.id.etReturnNote);
        btnSubmitReturn = findViewById(R.id.btnSubmitReturn);

        btnSubmitReturn.setOnClickListener(v -> submitReturnRequest());

        spinnerBorrowTickets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= activeTickets.size()) {
                    BorrowTicket ticket = activeTickets.get(position - 1);
                    selectedBorrowTicketId = ticket.getId();
                    loadTicketDevices(selectedBorrowTicketId);
                } else {
                    selectedBorrowTicketId = -1;
                    cardDevices.setVisibility(View.GONE);
                    borrowItemAdapter.setItems(new ArrayList<>());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBorrowTicketId = -1;
                cardDevices.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        rvReturnDevices.setLayoutManager(new LinearLayoutManager(this));
        borrowItemAdapter = new BorrowItemAdapter(false, null);
        rvReturnDevices.setAdapter(borrowItemAdapter);
    }

    private void loadActiveTickets() {
        int userId = session.getAccountId();
        
        // Load all borrow tickets of user
        borrowPresenter.loadUserTickets(userId, new BorrowPresenter.BorrowListView() {
            @Override
            public void onTicketsLoaded(List<BorrowTicket> tickets) {
                activeTickets.clear();
                // Filter only tickets that are borrowed or overdue (devices are with user)
                for (BorrowTicket t : tickets) {
                    if (Constants.BORROW_BORROWED.equals(t.getStatus()) || Constants.BORROW_OVERDUE.equals(t.getStatus()) || Constants.BORROW_PARTIALLY_RETURNED.equals(t.getStatus())) {
                        activeTickets.add(t);
                    }
                }
                populateSpinner();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReturnCreateActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateSpinner() {
        List<String> displayList = new ArrayList<>();
        displayList.add("-- Chọn phiếu mượn --");
        for (BorrowTicket t : activeTickets) {
            String statusText = Constants.BORROW_OVERDUE.equals(t.getStatus()) ? " (QUÁ HẠN)" : "";
            displayList.add(t.getTicketCode() + " - Hạn: " + t.getExpectedReturnDate() + statusText);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, displayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(android.graphics.Color.WHITE);
                }
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(android.graphics.Color.WHITE);
                    ((TextView) view).setBackgroundColor(android.graphics.Color.parseColor("#1C1C1E"));
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBorrowTickets.setAdapter(adapter);
    }

    private void applyDarkTheme() {
        if (!com.haui.devicemanagement.util.ThemeManager.isDarkMode(this)) return;
        android.view.View root = findViewById(R.id.rootLayout);
        if (root != null) {
            root.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
        }
        android.view.View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(android.graphics.Color.parseColor("#161616"));
            if (toolbar instanceof Toolbar && ((Toolbar) toolbar).getNavigationIcon() != null) {
                androidx.core.graphics.drawable.DrawableCompat.setTint(
                    androidx.core.graphics.drawable.DrawableCompat.wrap(((Toolbar) toolbar).getNavigationIcon()), 
                    android.graphics.Color.WHITE
                );
            }
        }
        android.view.View layoutSubmit = findViewById(R.id.layoutSubmit);
        if (layoutSubmit != null) {
            layoutSubmit.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
        }

        // Style the TextInputLayout
        com.google.android.material.textfield.TextInputLayout tilReturnNote = findViewById(R.id.tilReturnNote);
        int boxColor = android.graphics.Color.parseColor("#444446");
        int hintColorVal = android.graphics.Color.parseColor("#8E8E8E");
        android.content.res.ColorStateList stateList = android.content.res.ColorStateList.valueOf(hintColorVal);

        if (tilReturnNote != null) {
            tilReturnNote.setBoxStrokeColor(boxColor);
            tilReturnNote.setBoxStrokeColorStateList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1962D1")));
            tilReturnNote.setDefaultHintTextColor(stateList);
            tilReturnNote.setHintTextColor(stateList);
        }

        android.view.ViewGroup rootLayout = findViewById(R.id.rootLayout);
        if (rootLayout != null) {
            applyDarkThemeToViewGroup(rootLayout);
        }
    }

    private void applyDarkThemeToViewGroup(android.view.ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            android.view.View child = viewGroup.getChildAt(i);
            if (child instanceof androidx.cardview.widget.CardView) {
                ((androidx.cardview.widget.CardView) child).setCardBackgroundColor(
                        android.graphics.Color.parseColor("#1C1C1E"));
                applyDarkThemeToViewGroup((android.view.ViewGroup) child);
            } else if (child instanceof android.widget.EditText) {
                android.widget.EditText et = (android.widget.EditText) child;
                et.setTextColor(android.graphics.Color.WHITE);
                et.setHintTextColor(android.graphics.Color.parseColor("#8E8E8E"));
            } else if (child instanceof android.widget.TextView) {
                android.widget.TextView tv = (android.widget.TextView) child;
                if (tv.getId() == R.id.btnSubmitReturn) {
                    continue;
                }
                String text = tv.getText().toString();
                if (text.contains("Chọn phiếu mượn cần trả") || 
                    text.contains("Thiết bị sẽ trả") || 
                    text.contains("Ghi chú trả thiết bị")) {
                    tv.setTextColor(android.graphics.Color.WHITE);
                } else {
                    tv.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
                }
            } else if (child instanceof android.view.ViewGroup) {
                applyDarkThemeToViewGroup((android.view.ViewGroup) child);
            }
        }
    }

    private void loadTicketDevices(int ticketId) {
        borrowPresenter.loadTicketDetail(ticketId, new BorrowPresenter.BorrowDetailView() {
            @Override
            public void onTicketLoaded(BorrowTicket ticket, List<BorrowItem> items) {
                cardDevices.setVisibility(View.VISIBLE);
                borrowItemAdapter.setItems(items);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReturnCreateActivity.this, "Lỗi khi tải chi tiết: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReturnRequest() {
        if (selectedBorrowTicketId == -1) {
            Toast.makeText(this, "Vui lòng chọn phiếu mượn cần trả", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = session.getAccountId();
        String note = etReturnNote.getText() != null ? etReturnNote.getText().toString().trim() : "";

        btnSubmitReturn.setEnabled(false);
        returnPresenter.createReturnTicket(userId, selectedBorrowTicketId, new ReturnPresenter.ReturnActionView() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(ReturnCreateActivity.this, message, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message) {
                btnSubmitReturn.setEnabled(true);
                Toast.makeText(ReturnCreateActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}