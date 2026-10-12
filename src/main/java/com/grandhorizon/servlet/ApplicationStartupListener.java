package com.grandhorizon.servlet;

import com.grandhorizon.util.FileStorageUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;

@WebListener
public class ApplicationStartupListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            FileStorageUtil.getUsersFile();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to initialize the user data file.", e);
        }
    }
}
