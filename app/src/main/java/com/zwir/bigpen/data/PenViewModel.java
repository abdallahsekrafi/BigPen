package com.zwir.bigpen.data;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class PenViewModel extends AndroidViewModel {

    private LiveData<List<Pen>> penList;

    private BigPenDatabase bigPenDatabase;
    private static final String delete = "delete", insert = "insert";

    public PenViewModel(Application application) {
        super(application);
        bigPenDatabase = BigPenDatabase.getDatabase(this.getApplication());
        penList = bigPenDatabase.getPenDao().getAllPen();
    }

    public LiveData<List<Pen>> getPenList() {
        return penList;
    }
    public void deletePen(Pen pen) {
        new PenDataOperation(bigPenDatabase,delete).execute(pen);
    }
    public void savePen(Pen pen) {
        new PenDataOperation(bigPenDatabase,insert).execute(pen);
    }

    private static class PenDataOperation extends AsyncTask<Pen, Void, Void> {

        private BigPenDatabase bigPenDatabase;
        private String operation;

        PenDataOperation(BigPenDatabase bigPenDatabase, String operation) {
            this.bigPenDatabase = bigPenDatabase;
            this.operation = operation;
        }

        @Override
        protected Void doInBackground(final Pen... params) {

            switch (operation) {
                case insert:
                  bigPenDatabase.getPenDao().savePen(params[0]);
                    break;
                case delete:
                    bigPenDatabase.getPenDao().deletePen(params[0]);
                    break;
            }
            return null;
            }


    }
}

