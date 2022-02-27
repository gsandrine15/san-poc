package com.trss.bi.triggers;

import org.h2.tools.TriggerAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteUserRoleOnUserSoftDeleteTrigger extends TriggerAdapter {

    @Override
    public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
        if (oldRow.getBoolean("deleted") != newRow.getBoolean("deleted") && newRow.getBoolean("deleted")) {
            conn.createStatement().execute("DELETE FROM jhi_user_role where user_id = ?", new String[]{newRow.getString("id")});
            conn.commit();
            conn.close();
        }
    }
}
