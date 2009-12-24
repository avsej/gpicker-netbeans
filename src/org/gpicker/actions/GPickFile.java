/*
 *    Copyright: This program is free software: you can redistribute it and/or
 *               modify it under the terms of the GNU General Public License as
 *               published by the Free Software Foundation; either version 3 of the
 *               License, or (at your option) any later version.
 *
 *               This program is distributed in the hope that it will be useful,
 *               but WITHOUT ANY WARRANTY; without even the implied warranty of
 *               MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *               General Public License for more details.
 *
 *               You should have received a copy of the GNU General Public License
 *               along with this program.  If not, see
 *               `http://www.gnu.org/licenses/'.
 */
package org.gpicker.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public final class GPickFile implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            FileObject file = EditorContextDispatcher.getDefault().getCurrentFile();
            Project project;
            if (file != null) {
                project = FileOwnerQuery.getOwner(file);
            } else {
                OpenProjects op = OpenProjects.getDefault();
                project = op.getMainProject();
                if (project == null && op.getOpenProjects().length > 0) {
                    project = op.getOpenProjects()[0];
                }
            }

            if (project != null) {
                Process p = Runtime.getRuntime().exec("gpicker -t guess " + project.getProjectDirectory().getPath());

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    file = project.getProjectDirectory().getFileObject(s);
                    if (file != null) {
                        OpenCookie open = DataObject.find(file).getCookie(OpenCookie.class);
                        if (open != null) {
                            open.open();
                        }
                    }
                }
                if ((s = stdError.readLine()) != null) {
                    System.err.println(s);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
