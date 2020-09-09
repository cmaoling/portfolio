package name.abuchen.portfolio.ui.jobs;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import name.abuchen.portfolio.model.Adaptor;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.PortfolioPlugin;
import name.abuchen.portfolio.ui.editor.ClientInput;

public final class AutoSaveJob extends AbstractClientJob
{
    private ClientInput clientInput;
    private Object      clazz;
    private long        heartbeatPeriod;

    public AutoSaveJob(ClientInput clientInput, Object clazz)
    {
        super(clientInput.getClient(), Messages.JobLabelAutoSave);
        this.clientInput = clientInput;
        this.clazz = clazz;
    }

    public AutoSaveJob setHeartbeat(long milliseconds)
    {
        heartbeatPeriod = milliseconds;
        return this;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
        long delay = getDelay();
        // 0 means not to autosave at all
        if (delay != 0L)
        {
            this.clientInput.autoSave();
            PortfolioPlugin.info(MessageFormat.format(Messages.JobMsgAutoSave, this.clientInput.getFile(), this.clientInput.getAutoSaveFile(), delay));
            scheduleMinutes(delay);
        }
        else
            schedule(heartbeatPeriod);
        return Status.OK_STATUS;
    }

    public void scheduleMinutes(long delay)
    {
        super.schedule((long) delay  * 60000); // value is given in minutes
    }

    public long getDelay()
    {
        Object value;
        PropertyDescriptor descriptor = null;
        Object attributable = null;
        try
        {
            descriptor = descriptorFor(clazz.getClass(), "autosavePeriod"); //$NON-NLS-1$
            attributable = Adaptor.adapt(clazz.getClass(), clazz);
            value = descriptor.getReadMethod().invoke(attributable);
        }
        catch (Exception e)
        {
            throw new RuntimeException(String.format("Descriptor failed with exception <%s>", e)); //$NON-NLS-1$
        }
        return (long) value;
    }
//
//    @Depreciated
//    public void setDelay(long delay)
//    {
//        this.delay = delay;
//    }

    protected PropertyDescriptor descriptorFor(Class<?> subjectType, String attributeName)
    {
        try
        {
            PropertyDescriptor[] properties = Introspector.getBeanInfo(subjectType).getPropertyDescriptors();
            for (PropertyDescriptor p : properties)
                if (attributeName.equals(p.getName()))
                    return p;
            throw new IllegalArgumentException(String.format("%s has no property named %s", subjectType //$NON-NLS-1$
                            .getName(), attributeName));
        }
        catch (IntrospectionException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

}
