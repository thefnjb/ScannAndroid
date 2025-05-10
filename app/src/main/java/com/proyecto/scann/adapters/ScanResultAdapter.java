package com.proyecto.scann.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyecto.scann.R;
import com.proyecto.scann.modelos.ScanResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {

    private List<ScanResult> scanResults;
    private OnItemDeleteListener deleteListener;
    private OnItemCopyListener copyListener;
    private OnItemClickListener clickListener; // Nuevo: clic normal

    // Interfaz para eliminar ítems
    public interface OnItemDeleteListener {
        void onDeleteClick(int position, String scanId);
    }

    // Interfaz para copiar datos
    public interface OnItemCopyListener {
        void onCopyClick(String dataToCopy);
    }

    // Nuevo: Interfaz para clic normal
    public interface OnItemClickListener {
        void onItemClick(String scanData);
    }

    // Constructor que recibe los 3 listeners
    public ScanResultAdapter(List<ScanResult> scanResults,
                             OnItemDeleteListener deleteListener,
                             OnItemCopyListener copyListener,
                             OnItemClickListener clickListener) {
        this.scanResults = scanResults;
        this.deleteListener = deleteListener;
        this.copyListener = copyListener;
        this.clickListener = clickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setScanResults(List<ScanResult> newScanResults) {
        this.scanResults.clear();
        this.scanResults.addAll(newScanResults);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < scanResults.size()) {
            scanResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scan_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ScanResult scanResult = scanResults.get(position);

        holder.tvScanData.setText(scanResult.getData());

        // Formatear fecha a zona horaria Lima
        Date date = new Date(scanResult.getFecha() * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        TimeZone timeZone = TimeZone.getTimeZone("America/Lima");
        sdf.setTimeZone(timeZone);
        holder.tvScanFecha.setText("Fecha: " + sdf.format(date));

        // Evento eliminar
        holder.ivDeleteScan.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(position, scanResult.getId());
            }
        });

        // Evento copiar (mantener presionado)
        holder.itemView.setOnLongClickListener(v -> {
            if (copyListener != null) {
                copyListener.onCopyClick(scanResult.getData());
                return true;
            }
            return false;
        });

        // Evento clic normal
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(scanResult.getData());
            }
        });
    }

    @Override
    public int getItemCount() {
        return scanResults != null ? scanResults.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvScanData;
        TextView tvScanFecha;
        ImageView ivDeleteScan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScanData = itemView.findViewById(R.id.tvScanData);
            tvScanFecha = itemView.findViewById(R.id.tvScanFecha);
            ivDeleteScan = itemView.findViewById(R.id.ivDeleteScan);
        }
    }
}
