package org.jpos.qi.minigl;

import com.vaadin.data.Binder;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.jpos.gl.Journal;
import org.jpos.gl.Layer;
import org.jpos.qi.QIEntityView;
import org.jpos.qi.QIHelper;

import java.util.List;

public class LayersView extends QIEntityView {
    private Journal selectedJournal;

    public LayersView() {
        super(Layer.class);
    }

    @Override
    protected HorizontalLayout createHeader (String title) {
        HorizontalLayout header;
        if (isGeneralView()) {
            VerticalLayout main = new VerticalLayout();
            main.setMargin(false);
            HorizontalLayout hl = super.createHeader(title);
            main.addComponent(hl);
            main.addComponent(createJournalPanel());
            header = new HorizontalLayout(main);
        } else {
            header = super.createHeader(title);
        }
        header.setWidth("100%");
        return header;
    }

    @Override
    public void setGridGetters() {
        Grid<Layer> g = getGrid();
        g.addColumn(Layer::getJournal).setId("journal");
        g.addColumn(Layer::getId).setId("id");
        g.addColumn(Layer::getName).setId("name");
    }

    @Override
    public QIHelper createHelper() {
        return new LayersHelper();
    }

    @Override
    public Object getEntity(Object entity) {
        if(entity instanceof Layer) {
            Layer l = (Layer) entity;
            if(l.getId() > 0) {
                return getHelper().getEntityByParam(String.valueOf(l.getId()));
            }
        }
        return null;
    }

    @Override
    public String getHeaderSpecificTitle(Object entity) {
        if (entity instanceof Layer) {
            Layer l = (Layer) entity;
            return l.getId() > 0 && l.getName() != null ?
                    l.getId() + " " + l.getName() :getApp().getMessage("new");
        } else {
            return null;
        }
    }

    private HorizontalLayout createJournalPanel() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setMargin(new MarginInfo(false,true,true,true));
        hl.setSpacing(true);
        ComboBox<Journal> box = createJournalBox();
        box.setStyleName(ValoTheme.COMBOBOX_SMALL);
        box.addValueChangeListener(listener -> {
            ConfigurableFilterDataProvider wrapper = (ConfigurableFilterDataProvider) getGrid().getDataProvider();
            wrapper.setFilter(listener.getValue());
            wrapper.refreshAll();
            this.selectedJournal = listener.getValue();
        });
        hl.addComponent(box);
        return hl;
    }

    private ComboBox<Journal> createJournalBox() {
        ComboBox<Journal> box = new ComboBox(getApp().getMessage("journal"));
        box.setItemCaptionGenerator(Journal::getName);
        List<Journal> jList = ((LayersHelper)getHelper()).getAllJournals();
        box.setItems(jList);
        box.setEmptySelectionAllowed(false);
        if (this.selectedJournal != null) {
            box.setValue(selectedJournal);
        } else {
            if (jList.size() > 0) {
                box.setValue(jList.get(0));
                this.selectedJournal = jList.get(0);
            }
        }
        return box;
    }

    @Override
    public void showSpecificView (final String parameter) {
        String[] params = parameter.split("\\?journal=");
        if (params.length > 1) {
            String journalId = params[1];
            try {
                this.selectedJournal = ((LayersHelper)getHelper()).getJournal(journalId);
            } catch (Exception e) {
                getApp().getLog().error(e);
            }
            super.showSpecificView(parameter);
        } else if (parameter.contains("new")){
            getApp().displayError("Invalid Journal","Must select a Journal");
            getApp().getNavigator().navigateTo(getGeneralRoute());
        } else {
            super.showSpecificView(parameter);
        }

    }

    protected Component buildAndBindCustomComponent(String propertyId) {
        if ("journal".equalsIgnoreCase(propertyId)) {
            ComboBox<Journal> comboBox = createJournalBox();
            comboBox.setEnabled(false);
            Binder.BindingBuilder builder = getBinder()
                    .forField(comboBox);
            if(isRequired(propertyId))
                builder.asRequired(getApp().getMessage("errorMessage.req",getApp().getMessage(propertyId)));
            builder.bind(propertyId);
            ((Layer)getInstance()).setJournal(this.selectedJournal);
            return comboBox;
        }
        return null;
    }

    @Override
    protected void navigateToNewRoute() {
        getApp().getNavigator().navigateTo(getGeneralRoute() + "/new?journal=" + this.selectedJournal.getId());
    }

    @Override
    protected void navigateToSpecificView(Grid.ItemClick event) {
        String url = getGeneralRoute() + "/" + getHelper().getItemId(event.getItem())
                + "?journal=" + this.selectedJournal.getId();
        getApp().getNavigator().navigateTo(url);
    }

    @Override
    public boolean canEdit() {
        return true;
    }

    @Override
    public boolean canAdd() {
        return true;
    }

    @Override
    public boolean canRemove() {
        return true;
    }
}
