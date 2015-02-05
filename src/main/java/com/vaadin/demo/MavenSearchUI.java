package com.vaadin.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderer.HtmlRenderer;

@Theme("maven-search")
@Widgetset("com.vaadin.DefaultWidgetSet")
@SuppressWarnings("serial")
public class MavenSearchUI extends UI {

    private Grid grid;
    private TextField search;
    private VerticalLayout layout;

    private static final String URI_FRAGMENT_PREFIX = "search:";

    @Override
    protected void init(VaadinRequest request) {
        layout = new VerticalLayout();
        layout.setHeight("600px");
        layout.setSpacing(true);
        setContent(layout);

        search = new TextField();
        search.setInputPrompt("Search for Maven packages...");
        search.setWidth("100%");
        search.addTextChangeListener(new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                doSearch(event.getText());
            }

        });

        grid = new Grid();
        grid.setVisible(false);
        layout.addComponents(search, grid);
        layout.setExpandRatio(grid, 1.0f);

        String uriFragment = Page.getCurrent().getUriFragment();
        if (uriFragment != null && uriFragment.startsWith(URI_FRAGMENT_PREFIX)) {
            doSearch(uriFragment.substring(URI_FRAGMENT_PREFIX.length()));
        } else {
            doSearch("vaadin");
        }
    }

    private void updateUriFragment(String searchTerms) {
        if (searchTerms == null || searchTerms.length() == 0) {
            Page.getCurrent().setUriFragment("", false);
        } else {
            String uriFragment = Page.getCurrent().getUriFragment();
            if (!(URI_FRAGMENT_PREFIX + searchTerms).equals(uriFragment)) {
                Page.getCurrent().setUriFragment(
                        URI_FRAGMENT_PREFIX + searchTerms, false);
            }
        }
    }

    private void doSearch(String searchTerms) {
        if (searchTerms.length() < 3) {
            return;
        }
        updateUriFragment(searchTerms);
        search.setValue(searchTerms);

        // Create a new Grid to workaround an NPE caused by updating container
        // data source.
        Grid newGrid = new Grid();
        newGrid.addStyleName("search-results");
        newGrid.setSizeFull();
        newGrid.setSelectionMode(SelectionMode.NONE);
        newGrid.setContainerDataSource(new LazySearchContainer(searchTerms));

        // Configure columns properly.
        newGrid.getColumn("g").setHeaderCaption("groupId").setWidth(318.0)
                .setRenderer(new HtmlRenderer(), new GroupIdHtmlConverter());
        newGrid.getColumn("a").setWidth(288).setHeaderCaption("artifactId");
        newGrid.getColumn("latestVersion").setHeaderCaption("version")
                .setWidth(100.0);
        newGrid.getColumn("timestamp").setHeaderCaption("updated")
                .setWidth(170.0)
                .setRenderer(new HtmlRenderer(), new UpdatedHtmlConverter());
        newGrid.getColumn("javaDocUrl").setHeaderCaption("").setWidth(90.0)
                .setRenderer(new HtmlRenderer(), new JavaDocHtmlConverter());

        layout.replaceComponent(grid, newGrid);
        grid = newGrid;
    }
}
