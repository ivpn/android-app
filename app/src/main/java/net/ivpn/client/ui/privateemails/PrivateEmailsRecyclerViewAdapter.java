package net.ivpn.client.ui.privateemails;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ivpn.client.R;
import net.ivpn.client.databinding.EmailItemBinding;
import net.ivpn.client.rest.data.privateemails.Email;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PrivateEmailsRecyclerViewAdapter
        extends RecyclerView.Adapter<PrivateEmailsRecyclerViewAdapter.PrivateEmailViewHolder> {

    private List<Email> emails = new LinkedList<>();
    private PrivateEmailsNavigator navigator;
    private Email lastUpdatedEmail;

    PrivateEmailsRecyclerViewAdapter(PrivateEmailsNavigator navigator) {
        this.navigator = navigator;
    }

    @NonNull
    @Override
    public PrivateEmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        EmailItemBinding binding = EmailItemBinding.inflate(layoutInflater, parent, false);
        return new PrivateEmailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PrivateEmailViewHolder holder, int position) {
        holder.bind(emails.get(position));
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    public void setEmails(List<Email> emails) {
        this.emails = new ArrayList<>(emails);
        notifyDataSetChanged();
    }

    class PrivateEmailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private EmailItemBinding binding;
        private Email email;
        private boolean isInAnimation;
        ColorDrawable[] BackGroundColor = {
                new ColorDrawable(Color.parseColor("#e5e5e5")),
                new ColorDrawable(Color.parseColor("#ffffff"))
        };

        PrivateEmailViewHolder(EmailItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.contentLayout.setOnClickListener(this);
        }

        public void bind(Email email) {
            this.email = email;
            binding.setEmail(email);
            binding.setNavigator(navigator);
            binding.executePendingBindings();

            if (lastUpdatedEmail != null && lastUpdatedEmail.equals(email)) {
                isInAnimation = true;
                TransitionDrawable transitiondrawable = new TransitionDrawable(BackGroundColor);
                binding.animationLayer.setBackground(transitiondrawable);
                transitiondrawable.startTransition(1500);
                lastUpdatedEmail = null;
            } else if (isInAnimation) {
                isInAnimation = false;
                binding.animationLayer.setBackground(null);
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.content_layout: {
                    navigator.editEmail(email);
                    break;
                }
            }
        }
    }
}