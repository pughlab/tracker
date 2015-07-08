angular
  .module 'tracker.admin'

  .factory 'renderAuditRecord', () ->

    return (record) ->

      switch record.event_type
        when "new_case"
          "Added a new case: #{record.identifier}" 
        when "set_value"
          oldValue = if record.args.old?['$notAvailable'] then "N/A" else record.args.old
          newValue = if record.args.value?['$notAvailable'] then "N/A" else record.args.value
          "Changed #{record.attribute} for #{record.identifier} from #{oldValue} to #{newValue}"
