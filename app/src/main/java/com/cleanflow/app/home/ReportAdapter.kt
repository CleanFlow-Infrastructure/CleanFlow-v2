package com.cleanflow.app.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cleanflow.app.R
import com.cleanflow.app.databinding.ItemReportBinding
import com.cleanflow.app.models.Report

class ReportAdapter(private val reports: List<Report>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.binding.tvIssueType.text = report.issueType
        holder.binding.tvCommunity.text = "${timeAgo(report.submittedAt)} • ${report.communityName}"
        holder.binding.tvStatus.text = report.status

        val (badgeRes, textColorRes) = when (report.status) {
            "Assigned" -> Pair(R.drawable.badge_assigned, R.color.status_assigned_text)
            "Resolved" -> Pair(R.drawable.badge_resolved, R.color.status_resolved_text)
            else -> Pair(R.drawable.badge_pending, R.color.status_pending_text)
        }
        holder.binding.tvStatus.setBackgroundResource(badgeRes)
        holder.binding.tvStatus.setTextColor(ContextCompat.getColor(holder.binding.root.context, textColorRes))

        // Image Evidence Thumbnail (Supports both local drawable resources and web URLs)
        if (!report.imageUrl.isNullOrEmpty()) {
            val context = holder.binding.root.context
            val resId = context.resources.getIdentifier(report.imageUrl, "drawable", context.packageName)
            holder.binding.cardImageContainer.visibility = View.VISIBLE
            if (resId != 0) {
                holder.binding.ivReportImage.load(resId) {
                    crossfade(true)
                }
            } else {
                holder.binding.ivReportImage.load(report.imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_water_drop)
                    error(R.drawable.ic_water_drop)
                }
            }
        } else {
            holder.binding.cardImageContainer.visibility = View.GONE
        }
    }

    override fun getItemCount() = reports.size

    private fun timeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val hours = diff / (1000 * 60 * 60)
        val days = hours / 24
        return when {
            hours < 1 -> "Just now"
            hours < 24 -> "$hours hours ago"
            else -> "$days days ago"
        }
    }
}