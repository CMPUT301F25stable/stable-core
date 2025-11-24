package com.example.eventlottery.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.eventlottery.R;
import com.example.eventlottery.users.User;

import java.util.ArrayList;

/**
 * Adapter for a user class: we want it to display the user's name & their id
 * References: https://medium.com/mindorks/custom-array-adapters-made-easy-b6c4930560dd
 */
public class UserAdapter extends ArrayAdapter<User> {
    private ArrayList<User> userList = new ArrayList<>();
    private Context context;

    /**
     * Constructor for a UserAdapter.
     * @param context The context it's made in
     * @param users The arraylist of users to display.
     */
    public UserAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
        this.context = context;
        this.userList = users;
    }

    /**
     * Sets user_list_item.xml's text fields
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return The fully populated {@link View} corresponding to the given position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.user_list_item, parent, false);
        }
        // Get user, the text fields, & set the text
        User user = userList.get(position);
        TextView userNameText = convertView.findViewById(R.id.userNameText);
        TextView userIdText = convertView.findViewById(R.id.userIdText);
        userNameText.setText(user.getName());
        userIdText.setText(user.getId());
        return convertView;
    }
}
